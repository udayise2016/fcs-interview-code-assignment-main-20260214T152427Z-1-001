# Code Coverage Tracking with JaCoCo

## Overview
This project uses JaCoCo (Java Code Coverage) to track and enforce code coverage metrics. The configuration ensures a minimum of 80% code coverage across all critical components.

## Configuration Details

### Maven Configuration
The `pom.xml` includes JaCoCo Maven plugin with the following settings:

```xml
<properties>
    <jacoco.version>0.8.8</jacoco.version>
    <jacoco.coverage.minimum>0.80</jacoco.coverage.minimum>
</properties>
```

### Coverage Rules
- **Instruction Coverage**: Minimum 80%
- **Branch Coverage**: Minimum 80%
- **Class Coverage**: 0 missed classes allowed
- **Method Coverage**: 0 missed methods allowed

## Running Coverage Analysis

### 1. Run Tests with Coverage
```bash
./mvnw clean test
```

### 2. Generate Coverage Report
```bash
./mvnw jacoco:report
```

### 3. Check Coverage Thresholds
```bash
./mvnw jacoco:check
```

### 4. Combined Test and Coverage
```bash
./mvnw clean test jacoco:report
```

## Coverage Reports

### Report Locations
- **HTML Report**: `target/site/jacoco/index.html`
- **XML Report**: `target/site/jacoco/jacoco.xml`
- **CSV Report**: `target/site/jacoco/jacoco.csv`

### Report Types

#### HTML Report (Recommended)
- Interactive, browser-based report
- Color-coded coverage visualization
- Drill-down capability to method level
- Coverage percentages by package, class, method

#### XML Report
- Machine-readable format
- Used by CI/CD tools
- Contains detailed coverage metrics

#### CSV Report
- Spreadsheet-compatible format
- Raw coverage data
- Useful for custom analysis

## Coverage Targets by Component

### Critical Business Logic (Target: 90%+)
- **CreateWarehouseUseCase**: All validation paths
- **ArchiveWarehouseUseCase**: Archive operations
- **ReplaceWarehouseUseCase**: Replacement logic
- **LocationGateway**: Location resolution

### Infrastructure Components (Target: 80%+)
- **WarehouseRepository**: Database operations
- **StoreTransactionEventListener**: Event handling
- **REST Controllers**: API endpoints

### Configuration and Utilities (Target: 70%+)
- **Domain Models**: Basic getters/setters
- **Configuration Classes**: Setup logic

## Coverage Enforcement

### Build Failure Conditions
The build will fail if:
- Instruction coverage < 80%
- Branch coverage < 80%
- Any classes are completely uncovered
- Any methods are completely uncovered

### Continuous Integration
```yaml
# Example CI configuration
coverage:
  status:
    project:
      default:
        target: 80%
    patch:
      default:
        target: 80%
```

## Coverage Analysis Workflow

### 1. Local Development
```bash
# Run tests with coverage
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### 2. Pre-commit Check
```bash
# Quick coverage check
./mvnw test jacoco:check
```

### 3. Pull Request Validation
```bash
# Full test suite with coverage
./mvnw clean test jacoco:report jacoco:check
```

## Improving Coverage

### 1. Identify Gaps
- Review HTML report for uncovered code
- Look for red highlighted lines
- Check missed branches in complex logic

### 2. Prioritize Critical Paths
- Focus on business logic first
- Add tests for validation rules
- Cover error handling paths

### 3. Test Strategy
- **Positive Tests**: Happy path scenarios
- **Negative Tests**: Error conditions
- **Edge Cases**: Boundary values
- **Integration Tests**: Component interactions

## Coverage Best Practices

### 1. Meaningful Coverage
- Focus on business logic, not boilerplate
- Test validation rules thoroughly
- Cover error handling paths

### 2. Avoid Coverage Gaming
- Don't write tests just for coverage
- Focus on test quality over quantity
- Maintain test readability

### 3. Regular Monitoring
- Review coverage trends
- Address coverage regressions
- Set realistic targets

## Troubleshooting

### Common Issues

#### 1. Coverage Below Threshold
```bash
# Check specific coverage
./mvnw jacoco:report

# Identify low coverage areas
open target/site/jacoco/index.html
```

#### 2. JaCoCo Agent Issues
```bash
# Clean and rebuild
./mvnw clean compile test-compile

# Ensure agent is prepared
./mvnw jacoco:prepare-agent test
```

#### 3. Report Generation Problems
```bash
# Force report regeneration
rm -rf target/site/jacoco
./mvnw jacoco:report
```

### Debug Mode
```bash
# Run with verbose output
./mvnw test -X jacoco:report
```

## Coverage Metrics Explained

### Instruction Coverage
- Percentage of bytecode instructions executed
- Most comprehensive coverage metric
- Good indicator of overall test coverage

### Branch Coverage
- Percentage of decision branches covered
- Important for complex conditional logic
- Higher than instruction coverage typically

### Class Coverage
- Percentage of classes with any coverage
- Ensures no completely untested classes
- Basic quality gate

### Method Coverage
- Percentage of methods with any coverage
- Ensures all public methods are tested
- Important for API coverage

## Integration with Development Tools

### IDE Integration
- **IntelliJ IDEA**: Built-in JaCoCo support
- **Eclipse**: EclEmma JaCoCo plugin
- **VS Code**: Coverage extensions available

### Build Tools
- **Maven**: jacoco-maven-plugin
- **Gradle**: jacoco plugin
- **CI/CD**: Various coverage integrations

## Reporting and Monitoring

### Dashboard Integration
- SonarQube integration
- Jenkins coverage plugins
- GitHub Actions coverage reporting

### Trend Analysis
- Track coverage over time
- Identify coverage regressions
- Set improvement targets

This comprehensive JaCoCo configuration ensures consistent code quality monitoring and enforces the 80% minimum coverage requirement across the Warehouse Management System.
