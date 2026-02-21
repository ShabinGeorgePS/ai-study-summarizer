# Contributing to AI Study Summarizer

First off, thank you for considering contributing to the AI Study Summarizer! It's people like you that make this such a great tool.

This project is open source and community-driven. We value your contributions, whether they're reporting bugs, suggesting features, improving documentation, or writing code.

---

## 📋 Code of Conduct

This project adheres to the Contributor Covenant [code of conduct](./CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior.

---

## 🚀 Getting Started

### Development Setup

1. **Fork and clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/ai-study-summarizer.git
cd ai-study-summarizer
```

2. **Create a development branch**
```bash
git checkout -b feature/your-feature-name
```

3. **Copy environment template**
```bash
cp .env.example .env
# Edit .env with your values
```

4. **Start PostgreSQL**
```bash
docker-compose up -d
```

5. **Start backend**
```bash
cd backend
./mvnw spring-boot:run
```

6. **Start frontend (new terminal)**
```bash
cd frontend
npm install
npm run dev
```

---

## 🐛 Reporting Bugs

Before creating bug reports, please check [existing issues](https://github.com/yourusername/ai-study-summarizer/issues) as you might find out the issue has already been reported.

**How to submit a good bug report:**
- Use a clear, descriptive title
- Describe the exact steps to reproduce the problem
- Describe the behavior you observed
- Explain what behavior you expected instead
- Include screenshots/screen recordings if possible
- Include your environment (OS, Java version, Node version, etc.)

---

## ✨ Suggesting Features

Feature suggestions are always welcome. When creating a feature request:

1. **Use a clear, descriptive title**
2. **Provide a detailed description** of the proposed feature
3. **Explain why this would be useful**
4. **List any similar features** in other tools
5. **Include mockups or wireframes** if applicable

---

## 💻 Pull Request Process

### Before You Start
- Read and follow the [Code of Conduct](./CODE_OF_CONDUCT.md)
- Check [existing PRs](https://github.com/yourusername/ai-study-summarizer/pulls)
- Follow the coding standards below
- Write tests for new functionality
- Update documentation if needed

### Creating a Pull Request

1. **Create a feature branch**
```bash
git checkout -b feature/description-of-feature
# or
git checkout -b fix/description-of-bug
```

2. **Make your changes**
- Keep commits atomic and logical
- Write clear commit messages

3. **Write/update tests**
```bash
# Backend
cd backend
./mvnw test

# Frontend
cd frontend
npm test
```

4. **Ensure code quality**
```bash
# Backend - check formatting
./mvnw spotless:check

# Frontend - lint
npm run lint
```

5. **Update documentation**
- Update README.md if needed
- Add/update code comments
- Document complex logic

6. **Push to your fork and submit PR**
```bash
git push origin feature/your-feature-name
```

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Closes #(issue number)

## Changes Made
- Change 1
- Change 2

## Testing
- [ ] Added unit tests
- [ ] Added integration tests
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Dependent changes merged and published
```

---

## 📝 Commit Message Guidelines

Use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that don't affect code meaning (formatting, etc)
- **refactor**: Code changes that don't fix bugs or add features
- **perf**: Code changes that improve performance
- **test**: Adding or updating tests
- **chore**: Changes to build process, dependencies, etc

### Examples
```
feat(auth): add password reset functionality
fix(upload): validate file size before processing
docs(readme): update installation instructions
test(summary): add unit tests for SummaryService
```

---

## 🎨 Code Style Guidelines

### Java (Backend)

- **Naming**: Follow Java conventions (camelCase for variables, PascalCase for classes)
- **Line Length**: Max 120 characters
- **Indentation**: 4 spaces (not tabs)
- **Formatting**: Use Google Java Format
  ```bash
  ./mvnw spotless:apply
  ```

**Example:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MyService {

    private final MyRepository repository;

    public MyDto doSomething(MyRequest request) {
        // Implementation with clear variable names
        // Add comments for complex logic
        return result;
    }
}
```

### JavaScript/React (Frontend)

- **Naming**: camelCase for variables/functions, PascalCase for components
- **Indentation**: 2 spaces
- **Semicolons**: Always use
- **Linting**: ESLint must pass
  ```bash
  npm run lint
  ```

**Example:**
```javascript
export const MyComponent = ({ prop1, prop2 }) => {
    const [state, setState] = useState(null);

    useEffect(() => {
        // Effect logic
    }, []);

    return (
        <div className="p-4">
            {/* JSX */}
        </div>
    );
};
```

---

## 🧪 Testing Requirements

### Backend Testing
- Minimum unit test coverage: 80%
- All services must have tests
- Use JUnit 5 + Mockito
- Create `*Test` or `*Tests` classes

```java
@SpringBootTest
class MyServiceTest {

    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyService service;

    @Test
    void testSuccessCase() {
        // Arrange
        // Act
        // Assert
    }
}
```

### Frontend Testing
- All utility functions must have tests
- Critical components should have tests
- Use Jest + React Testing Library

```javascript
describe('MyComponent', () => {
    test('renders correctly', () => {
        render(<MyComponent />);
        expect(screen.getByText(/text/i)).toBeInTheDocument();
    });
});
```

---

## 📚 Documentation Standards

When adding features, update:

1. **Code Comments** - Explain the "why" not the "what"
2. **README.md** - New features mentioned
3. **INTEGRATION.md** - New API endpoints documented
4. **JavaDoc** - Public methods documented
5. **JSDoc** - Complex functions documented

### Good Comment
```java
// Skip BOM (Byte Order Mark) if present in UTF-8 file
// Some tools add BOM which causes parsing issues
if (text.startsWith("\uFEFF")) {
    text = text.substring(1);
}
```

---

## 🔐 Security Guidelines

- **Never commit secrets** (.env, API keys, passwords)
- **Validate all inputs** (both frontend and backend)
- **Use parameterized queries** to prevent SQL injection
- **Sanitize HTML** to prevent XSS
- **Don't implement auth yourself** if possible, use proven libraries
- **Report security issues privately** - don't open public issues

See [TECHNICAL_AUDIT.md](./TECHNICAL_AUDIT.md) for security review guidelines.

---

## 🚫 What Not to Do

- ❌ Don't commit `.env` files
- ❌ Don't commit large files (>10MB)
- ❌ Don't make unrelated changes in one PR
- ❌ Don't update version/build numbers (maintainers do this)
- ❌ Don't introduce new dependencies without discussion
- ❌ Don't remove existing functionality without a feature request
- ❌ Don't commit commented-out code

---

## 📦 Adding Dependencies

Before adding a new dependency:

1. **Ask first** - Create an issue to discuss
2. **Justify it** - Why is it needed?
3. **Check alternatives** - Are there lighter options?
4. **Check size** - Will it bloat the bundle?
5. **Check maintenance** - Is it actively maintained?
6. **Check security** - Run `npm audit` or `dependency-check`

---

## 🎯 Project Areas

### 🔴 Priority 1 (Critical for Launch)
- Security hardening
- Error handling improvements
- Input validation
- Testing infrastructure

### 🟠 Priority 2 (Important for Beta)
- Database migrations
- API documentation
- Logging improvements
- Code cleanup

### 🟡 Priority 3 (Nice to Have)
- Performance optimizations
- UI/UX enhancements
- Advanced features
- Documentation improvements

**Want to help with Priority 1?** Comment on related issues!

---

## 🤔 Questions?

- **Development questions**: Open a [discussion](https://github.com/yourusername/ai-study-summarizer/discussions)
- **Setup issues**: Check [README.md](./README.md) troubleshooting section
- **General questions**: Email contributors or open an issue

---

## 🎉 Recognition

Contributors will be recognized in:
- README.md (Contributors section)
- Release notes
- GitHub contributors page

---

## 📖 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [Git Best Practices](https://git-scm.com/book/)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Thank you for contributing! Together we're making study better for everyone. 🚀**
