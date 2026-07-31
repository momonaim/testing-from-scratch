# React E2E Tests

This folder contains the end-to-end tests for the React frontend.

## Prerequisites

- Backend running at `http://localhost:8080`
- Frontend running at `http://localhost:5173`
- Node.js 18+

## Playwright

Install dependencies and browsers:

```bash
cd e2e-tests
npm install
npx playwright install
```

Run the suite:

```bash
npm test
```

Show the HTML report:

```bash
npm run report
```

## Selenium

Run the Java Selenium tests from the backend project:

```bash
cd task-manager
mvn test -Dtest=TaskUiSeleniumReactTest
```

## Notes

- Playwright is the primary recommendation for React E2E because it is designed for modern frontend apps and has stronger auto-waiting.
- Selenium is kept for comparison and interoperability with Java-based test stacks.
