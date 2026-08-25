import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Icon } from '../components/common/Icon';
import { useAuth } from '../auth/useAuth';

const loginDefaults = { email: '', password: '' };
const bootstrapDefaults = {
  email: '',
  employeeId: '',
  username: '',
  password: '',
  confirmPassword: '',
};

export function AuthPage() {
  const auth = useAuth();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState(loginDefaults);
  const [error, setError] = useState(auth.error);
  const [submitting, setSubmitting] = useState(false);

  const switchMode = (nextMode) => {
    setMode(nextMode);
    setForm(nextMode === 'login' ? loginDefaults : bootstrapDefaults);
    setError(null);
  };

  const updateField = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const submit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      if (mode === 'login') await auth.login(form);
      else await auth.bootstrap(form);
    } catch (submitError) {
      setError(submitError);
    } finally {
      setSubmitting(false);
    }
  };

  const isLogin = mode === 'login';

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="auth-title">
        <div className="auth-brand">
          <span className="brand-mark"><Icon name="assets" size={21} /></span>
          <div>
            <strong>AssetFlow</strong>
            <span>Lifecycle Management</span>
          </div>
        </div>

        <div className="auth-heading">
          <p className="eyebrow">Secure workspace</p>
          <h1 id="auth-title">{isLogin ? 'Sign in to AssetFlow' : 'Create the first account'}</h1>
          <p>
            {isLogin
              ? 'Use your employee account to access the asset workspace.'
              : 'Bootstrap is available only while the database has no application accounts.'}
          </p>
        </div>

        <div className="auth-tabs" role="tablist" aria-label="Authentication options">
          <button type="button" className={isLogin ? 'active' : ''} onClick={() => switchMode('login')}>
            Sign in
          </button>
          <button type="button" className={!isLogin ? 'active' : ''} onClick={() => switchMode('bootstrap')}>
            First account
          </button>
        </div>

        <form className="auth-form" onSubmit={submit}>
          <label>
            Employee email
            <input
              type="email"
              value={form.email}
              onChange={(event) => updateField('email', event.target.value)}
              autoComplete="email"
              required
            />
          </label>

          {!isLogin && (
            <>
              <label>
                Employee ID
                <input
                  value={form.employeeId}
                  onChange={(event) => updateField('employeeId', event.target.value)}
                  placeholder="The employee number already in the database"
                  required
                />
              </label>
              <label>
                Username
                <input
                  value={form.username}
                  onChange={(event) => updateField('username', event.target.value)}
                  autoComplete="username"
                  required
                />
              </label>
            </>
          )}

          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(event) => updateField('password', event.target.value)}
              autoComplete={isLogin ? 'current-password' : 'new-password'}
              required
            />
          </label>

          {!isLogin && (
            <label>
              Confirm password
              <input
                type="password"
                value={form.confirmPassword}
                onChange={(event) => updateField('confirmPassword', event.target.value)}
                autoComplete="new-password"
                required
              />
            </label>
          )}

          {error && (
            <div className="inline-error" role="alert">
              <Icon name="warning" size={16} />
              <span>{error.message}</span>
            </div>
          )}

          <Button type="submit" size="lg" disabled={submitting}>
            {submitting ? 'Connecting...' : isLogin ? 'Sign in' : 'Create and sign in'}
          </Button>
        </form>

        <p className="auth-footnote">Authentication uses an HTTP-only cookie and CSRF protection.</p>
      </section>
    </main>
  );
}
