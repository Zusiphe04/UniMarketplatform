const TESTIMONIALS = [
  {
    quote: 'I found affordable textbooks and saved so much. This platform is a game changer!',
    name: 'Lethabo M.',
    role: 'Student, CPUT',
    avatar: '/images/products/calculus-textbook.png',
  },
  {
    quote: "A great way to reach local students. I've grown my small business through Community Store.",
    name: 'Thabo N.',
    role: 'Local Vendor',
    avatar: '/images/services/haircut.png',
  },
  {
    quote: "It's safe, easy to use and really helps the community. Highly recommended!",
    name: 'Zinhle P.',
    role: 'Resident',
    avatar: '/images/services/braiding.png',
  },
];

export default function Testimonials() {
  return (
    <section className="testimonials-section" id="vendors" aria-labelledby="testimonials-title">
      <div className="container">
        <div className="testimonials-heading">
          <div>
            <p className="eyebrow eyebrow--line">Testimonials</p>
            <h2 id="testimonials-title">What our community says</h2>
          </div>
        </div>
        <div className="testimonial-grid">
          {TESTIMONIALS.map((testimonial) => (
            <figure className="testimonial-card" key={testimonial.name}>
              <blockquote>“{testimonial.quote}”</blockquote>
              <figcaption>
                <img src={testimonial.avatar} alt="" loading="lazy" />
                <span><strong>{testimonial.name}</strong><small>{testimonial.role}</small></span>
              </figcaption>
            </figure>
          ))}
        </div>
      </div>
    </section>
  );
}
