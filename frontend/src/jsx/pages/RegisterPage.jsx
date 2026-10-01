import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { apiRequest } from '../../js/api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import Icon from '../components/Icon.jsx';

const ACCOUNT_TYPES = [
  { value: 'BUYER', label: 'Community buyer', help: 'Shop, order and review.' },
  { value: 'STUDENT', label: 'Student', help: 'Verified campus persona.' },
  { value: 'FACULTY', label: 'Faculty', help: 'Verified institutional persona.' },
  { value: 'RESIDENT', label: 'Resident', help: 'Local community member.' },
  { value: 'VENDOR', label: 'Vendor', help: 'Apply to become a seller.' },
];

const QUALIFICATIONS = [
  { value: 'DIPLOMA', label: 'Diploma' },
  { value: 'BACHELORS_DEGREE', label: 'Bachelors Degree' },
  { value: 'ADVANCED_DIPLOMA', label: 'Advanced Diploma' },
  { value: 'MASTERS', label: 'Masters' },
  { value: 'HONORS', label: 'Honors' },
  { value: 'PHD', label: 'PhD' },
];

const STUDY_YEARS = [
  { value: '1', label: '1st' },
  { value: '2', label: '2nd' },
  { value: '3', label: '3rd' },
  { value: '4', label: '4th' },
  { value: '5', label: '5th' },
  { value: '6', label: '6th' },
];

const INITIAL_FORM = {
  firstName: '', lastName: '', displayName: '', email: '', password: '', confirmPassword: '',
  accountType: 'BUYER', organizationName: '', qualification: '', yearOfStudy: '',
  businessName: '', vendorDescription: '', websiteUrl: '', acceptedTerms: false, acceptedPrivacy: false,
};

export default function RegisterPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const requestedType = searchParams.get('type')?.toUpperCase();
  const [form, setForm] = useState({
    ...INITIAL_FORM,
    accountType: ACCOUNT_TYPES.some((item) => item.value === requestedType) ? requestedType : 'BUYER',
  });
  const [institutions, setInstitutions] = useState([]);
  const [institutionStatus, setInstitutionStatus] = useState('loading');
  const [institutionError, setInstitutionError] = useState('');
  const [institutionReloadKey, setInstitutionReloadKey] = useState(0);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (auth.isAuthenticated) navigate(auth.defaultRoute, { replace: true });
  }, [auth.isAuthenticated, auth.defaultRoute, navigate]);

  useEffect(() => {
    const controller = new AbortController();
    let active = true;
    const timeoutId = window.setTimeout(() => controller.abort(), 10000);
    setInstitutionStatus('loading');
    setInstitutionError('');

    apiRequest('/api/v1/institutions', { auth: false, signal: controller.signal })
      .then((payload) => {
        if (!Array.isArray(payload) || payload.length === 0) {
          throw new Error('The institution catalogue returned no institutions.');
        }
        if (!active) return;
        setInstitutions(payload);
        setInstitutionStatus('ready');
      })
      .catch((requestError) => {
        if (!active) return;
        setInstitutions([]);
        setInstitutionStatus('error');
        setInstitutionError(requestError.name === 'AbortError'
          ? 'The request timed out. Check that the backend is running, then try again.'
          : requestError.message || 'The institution catalogue could not be loaded.');
      })
      .finally(() => window.clearTimeout(timeoutId));

    return () => {
      active = false;
      window.clearTimeout(timeoutId);
      controller.abort();
    };
  }, [institutionReloadKey]);

  const needsInstitution = ['STUDENT', 'FACULTY'].includes(form.accountType);
  const isStudent = form.accountType === 'STUDENT';
  const isVendor = form.accountType === 'VENDOR';
  const selectedAccountType = ACCOUNT_TYPES.find((type) => type.value === form.accountType);
  const selectedInstitution = institutions.find((institution) => institution.name === form.organizationName);
  const emailPolicy = needsInstitution && selectedInstitution
    ? selectedInstitution[isStudent ? 'student' : 'faculty']
    : null;
  const academicLabel = isStudent ? 'student' : 'faculty';

  const updateField = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
    setFieldErrors((current) => {
      if (!current[name]) return current;
      const next = { ...current };
      delete next[name];
      return next;
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setFieldErrors({});

    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Passwords must match.' });
      return;
    }

    const normalizedEmail = form.email.trim();
    if (needsInstitution) {
      if (institutionStatus !== 'ready') {
        setError('Academic registration is unavailable until the institution catalogue has loaded.');
        return;
      }
      if (!selectedInstitution?.active) {
        setFieldErrors({ organizationName: 'Select an institution with email verification available.' });
        return;
      }
      if (!emailPolicy) {
        setFieldErrors({ email: `Email verification is not configured for this ${academicLabel} affiliation.` });
        return;
      }

      try {
        if (!new RegExp(emailPolicy.emailPattern, 'i').test(normalizedEmail)) {
          setFieldErrors({
            email: `Use your ${selectedInstitution.name} ${academicLabel} email. Expected format: ${emailPolicy.formatExample}.`,
          });
          return;
        }
      } catch {
        setError('This institution email policy could not be applied. Please try again later.');
        return;
      }
    }

    setSubmitting(true);
    try {
      const result = await auth.register({
        accountDetails: {
          email: normalizedEmail,
          password: form.password,
          firstName: form.firstName,
          lastName: form.lastName,
          displayName: form.displayName || null,
          acceptedTerms: form.acceptedTerms,
          acceptedPrivacy: form.acceptedPrivacy,
        },
        password: form.password,
        accountType: form.accountType,
        organizationName: form.organizationName,
        qualification: form.qualification,
        yearOfStudy: form.yearOfStudy,
        vendorDetails: {
          businessName: form.businessName,
          description: form.vendorDescription,
          websiteUrl: form.websiteUrl,
        },
      });
      navigate(result.destination, { replace: true, state: { newlyRegistered: true } });
    } catch (requestError) {
      setError(requestError.message);
      setFieldErrors(requestError.fieldErrors || {});
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="registration-page">
      <div className="container registration-layout">
        <section className="registration-intro">
          <p className="eyebrow eyebrow--line eyebrow--light">Join the movement</p>
          <h1>One community.<br /><em>Many ways to belong.</em></h1>
          <p>Create a secure account, choose how you participate, and we will guide you to the right workspace.</p>
          <ul>
            <li><Icon name="shield" size={17} /> Database-backed identity and sessions</li>
            <li><Icon name="store" size={17} /> Role-aware tools and permissions</li>
            <li><Icon name="spark" size={17} /> Local opportunity and measurable impact</li>
          </ul>
        </section>

        <section className="registration-form-card">
          <div className="auth-heading"><p className="eyebrow eyebrow--line">Get started</p><h2>Create your account</h2><p>Choose your account type and enter the details needed for your community profile.</p></div>
          <form className="platform-form platform-form--grid compact-auth-form" onSubmit={submit}>
            {error && <div className="form-alert form-alert--error form-span-2" role="alert">{error}</div>}
            {needsInstitution && institutionStatus === 'error' && (
              <div className="form-alert form-alert--error form-span-2" role="alert">
                Institution email verification could not be loaded: {institutionError}
                <button type="button" onClick={() => setInstitutionReloadKey((current) => current + 1)}>Try again</button>
              </div>
            )}
            <label>First name<input required maxLength="80" name="firstName" value={form.firstName} onChange={updateField} />{fieldErrors.firstName && <small>{fieldErrors.firstName}</small>}</label>
            <label>Last name<input required maxLength="80" name="lastName" value={form.lastName} onChange={updateField} />{fieldErrors.lastName && <small>{fieldErrors.lastName}</small>}</label>
            <label className="form-span-2">Display name <span>(optional)</span><input maxLength="120" name="displayName" value={form.displayName} onChange={updateField} /></label>
            <label className="form-span-2">{needsInstitution ? `${isStudent ? 'Student' : 'Faculty'} email address` : 'Email address'}
              <input
                required
                autoCapitalize="none"
                autoComplete="email"
                maxLength="254"
                name="email"
                pattern={emailPolicy?.emailPattern}
                placeholder={emailPolicy?.formatExample || ''}
                title={emailPolicy ? `Expected format: ${emailPolicy.formatExample}` : undefined}
                type="email"
                value={form.email}
                onChange={updateField}
              />
              {needsInstitution && (
                <span className="form-field-note">
                  {emailPolicy
                    ? `Expected ${academicLabel} email format: ${emailPolicy.formatExample}`
                    : 'Select an available institution to see its required academic email format.'}
                </span>
              )}
              {fieldErrors.email && <small>{fieldErrors.email}</small>}
            </label>
            <label>Password<input required autoComplete="new-password" minLength="12" maxLength="128" name="password" type="password" value={form.password} onChange={updateField} />{fieldErrors.password && <small>{fieldErrors.password}</small>}</label>
            <label>Confirm password<input required autoComplete="new-password" minLength="12" maxLength="128" name="confirmPassword" type="password" value={form.confirmPassword} onChange={updateField} />{fieldErrors.confirmPassword && <small>{fieldErrors.confirmPassword}</small>}</label>

            <label className="form-span-2">How will you use the platform?
              <select required name="accountType" value={form.accountType} onChange={updateField}>
                {ACCOUNT_TYPES.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}
              </select>
              <span className="form-field-note">{selectedAccountType?.help}</span>
            </label>

            {needsInstitution && <>
              <label className="form-span-2">Institution or university
                <select
                  required
                  aria-busy={institutionStatus === 'loading'}
                  name="organizationName"
                  value={form.organizationName}
                  onChange={updateField}
                >
                  <option value="">
                    {institutionStatus === 'loading'
                      ? 'Loading institutions…'
                      : institutionStatus === 'error'
                        ? 'Institution list unavailable — retry above'
                        : 'Select your institution'}
                  </option>
                  {institutions.map((institution) => (
                    <option key={institution.code} disabled={!institution.active} value={institution.name}>
                      {institution.name}{institution.active ? '' : ' — verification unavailable'}
                    </option>
                  ))}
                </select>
                <span className="form-field-note">
                  Unconfirmed institutions remain listed but cannot be selected until their official email format is verified.
                </span>
                {fieldErrors.organizationName && <small>{fieldErrors.organizationName}</small>}
              </label>
              <label className={isStudent ? '' : 'form-span-2'}>Qualification
                <select required name="qualification" value={form.qualification} onChange={updateField}>
                  <option value="">Select qualification</option>
                  {QUALIFICATIONS.map((qualification) => <option key={qualification.value} value={qualification.value}>{qualification.label}</option>)}
                </select>
              </label>
              {isStudent && <label>Year of study
                <select required name="yearOfStudy" value={form.yearOfStudy} onChange={updateField}>
                  <option value="">Select year</option>
                  {STUDY_YEARS.map((year) => <option key={year.value} value={year.value}>{year.label}</option>)}
                </select>
              </label>}
            </>}

            {isVendor && <>
              <label className="form-span-2">Business name<input required maxLength="160" name="businessName" value={form.businessName} onChange={updateField} /></label>
              <label className="form-span-2">Business description<textarea required maxLength="1000" name="vendorDescription" rows="3" value={form.vendorDescription} onChange={updateField} /></label>
              <label className="form-span-2">Website URL <span>(optional)</span><input maxLength="500" name="websiteUrl" type="url" value={form.websiteUrl} onChange={updateField} /></label>
            </>}

            <label className="check-label"><input required checked={form.acceptedTerms} name="acceptedTerms" type="checkbox" onChange={updateField} /><span>I accept the platform terms of use.</span></label>
            <label className="check-label"><input required checked={form.acceptedPrivacy} name="acceptedPrivacy" type="checkbox" onChange={updateField} /><span>I accept the privacy policy.</span></label>
            <button
              className="button button--orange form-submit form-span-2"
              type="submit"
              disabled={submitting || (needsInstitution && institutionStatus !== 'ready')}
            >
              {submitting ? 'Creating account…' : 'Create Account'} {!submitting && <Icon name="arrowRight" size={16} />}
            </button>
          </form>
          <p className="auth-switch">Already registered? <Link to="/sign-in">Sign in</Link></p>
        </section>
      </div>
    </main>
  );
}
