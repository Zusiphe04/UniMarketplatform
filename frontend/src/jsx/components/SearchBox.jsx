import Icon from './Icon.jsx';

export default function SearchBox({ value, onChange, onSubmit, compact = false, id = 'catalogue-search' }) {
  const submitSearch = (event) => {
    event.preventDefault();
    onSubmit(value.trim());
  };

  return (
    <form className={`search-box${compact ? ' search-box--compact' : ''}`} role="search" onSubmit={submitSearch}>
      <label className="sr-only" htmlFor={id}>Search products and services</label>
      <Icon name="search" size={19} />
      <input
        autoComplete="off"
        id={id}
        name="query"
        placeholder="Search campus finds..."
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
      <button className="search-box__submit" type="submit" aria-label="Search catalogue">
        <Icon name="arrowRight" size={18} />
      </button>
    </form>
  );
}
