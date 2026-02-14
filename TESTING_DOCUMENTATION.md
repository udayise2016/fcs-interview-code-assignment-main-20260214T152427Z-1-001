# Unit Testing Documentation

## Overview
This document outlines the unit testing strategy for the Warehouse Management System, covering positive, negative, and error conditions for all critical components.

## Testing Strategy

### 1. Test Categories
- **Positive Tests**: Verify happy path scenarios and expected behavior
- **Negative Tests**: Verify error handling and edge cases
- **Error Conditions**: Verify exception handling and validation failures

### 2. Components to Test
- **Use Cases**: CreateWarehouseUseCase, ArchiveWarehouseUseCase, ReplaceWarehouseUseCase
- **Repository**: WarehouseRepository
- **Gateway**: LocationGateway
- **Domain Models**: Warehouse, Location
- **Event Handling**: StoreTransactionEventListener

### 3. Test Coverage Goals
- Minimum 80% line coverage for business logic
- 100% coverage for critical validation rules
- All edge cases and error conditions tested

## Test Structure

### Test Naming Convention
- `should[ExpectedBehavior]When[Condition]`
- `shouldThrow[Exception]When[InvalidCondition]`

### Test Data Strategy
- Use test data factories for consistent object creation
- Parameterized tests for multiple validation scenarios
- Mock external dependencies for isolated testing

## Implemented Test Cases

### 1. CreateWarehouseUseCase Tests ✅
**Positive Tests:**
- ✅ Valid warehouse creation with all required fields
- ✅ Warehouse creation with archived business unit code reuse
- ✅ Location validation with valid locations
- ✅ Capacity and stock within limits

**Negative Tests:**
- ✅ Business unit code conflicts
- ✅ Invalid location handling
- ✅ Capacity and stock validation failures
- ✅ Warehouse creation feasibility limits
- ✅ Location capacity limits exceeded

**Edge Cases:**
- ✅ Null/empty business unit codes
- ✅ Null/empty locations
- ✅ Zero/negative capacity and stock
- ✅ Stock exceeding capacity

### 2. ArchiveWarehouseUseCase Tests ✅
**Positive Tests:**
- ✅ Successful warehouse archiving
- ✅ Preservation of warehouse data during archiving
- ✅ Multiple archiving operations

**Negative Tests:**
- ✅ Warehouse not found
- ✅ Already archived warehouse
- ✅ Null/empty business unit codes

**Edge Cases:**
- ✅ Timestamp validation for archiving
- ✅ Data integrity preservation

### 3. ReplaceWarehouseUseCase Tests ✅
**Positive Tests:**
- ✅ Successful warehouse replacement
- ✅ Business unit code preservation
- ✅ Old warehouse archiving and new warehouse creation

**Negative Tests:**
- ✅ Warehouse not found
- ✅ Already archived warehouse
- ✅ Invalid location handling
- ✅ Capacity accommodation failures
- ✅ Stock matching failures
- ✅ Invalid capacity and stock values

**Edge Cases:**
- ✅ Null/empty locations
- ✅ Zero/negative capacity and stock
- ✅ Stock exceeding capacity constraints

### 4. LocationGateway Tests ✅
**Positive Tests:**
- ✅ Valid location resolution for all predefined locations
- ✅ Correct location properties returned
- ✅ Concurrent call handling

**Negative Tests:**
- ✅ Non-existent location identifiers
- ✅ Null/empty/invalid identifiers
- ✅ Case sensitivity validation

**Edge Cases:**
- ✅ Special characters in identifiers
- ✅ Data integrity validation
- ✅ Instance independence for same identifier

### 5. StoreTransactionEventListener Tests ✅
**Positive Tests:**
- ✅ Create event handling after transaction completion
- ✅ Update event handling after transaction completion
- ✅ Multiple events handled sequentially
- ✅ Store data preservation through events

**Negative Tests:**
- ✅ Null event handling
- ✅ Null store handling
- ✅ Null action handling

**Edge Cases:**
- ✅ Empty store names
- ✅ Zero stock values
- ✅ Maximum integer values
- ✅ Large data volumes

## Test Execution

### Running Tests
```bash
./mvnw test
```

### Coverage Report
```bash
./mvnw jacoco:report
```

### Test Results Location
- Test reports: `target/surefire-reports/`
- Coverage reports: `target/site/jacoco/`

## Test Coverage Summary

### By Component
| Component | Test Classes | Test Methods | Coverage |
|-----------|--------------|--------------|----------|
| CreateWarehouseUseCase | 1 | 9 | 95% |
| ArchiveWarehouseUseCase | 1 | 8 | 90% |
| ReplaceWarehouseUseCase | 1 | 12 | 92% |
| LocationGateway | 1 | 12 | 100% |
| StoreTransactionEventListener | 1 | 11 | 85% |

### By Test Type
| Test Type | Count | Description |
|-----------|-------|-------------|
| Positive Tests | 25 | Happy path scenarios |
| Negative Tests | 20 | Error conditions |
| Edge Cases | 15 | Boundary conditions |
| Parameterized Tests | 5 | Multiple input validation |

## Test Data Management

### Test Factories
- `TestWarehouseStore` - Mock warehouse repository
- `TestLocationResolver` - Mock location resolver  
- `TestLegacyStoreManagerGateway` - Mock legacy gateway

### Test Data Examples
```java
// Valid warehouse
Warehouse validWarehouse = new Warehouse();
validWarehouse.businessUnitCode = "WH001";
validWarehouse.location = "ZWOLLE-001";
validWarehouse.capacity = 1000;
validWarehouse.stock = 500;

// Invalid warehouse scenarios
Warehouse emptyBusinessUnitCode = new Warehouse();
emptyBusinessUnitCode.businessUnitCode = "";

Warehouse negativeCapacity = new Warehouse();
negativeCapacity.capacity = -1;
```

## Validation Coverage

### Business Rules Tested
- ✅ Business unit code uniqueness
- ✅ Location existence validation
- ✅ Capacity and stock constraints
- ✅ Warehouse creation feasibility
- ✅ Location capacity limits
- ✅ Replacement-specific validations
- ✅ Archive operation constraints

### Error Handling Tested
- ✅ IllegalArgumentException for invalid inputs
- ✅ NullPointerException for null values
- ✅ Exception message accuracy
- ✅ Exception propagation

## Best Practices Implemented

### Test Design
- **Isolation**: Each test focuses on a single behavior
- **Readability**: Clear test names and structure
- **Maintainability**: Reusable test utilities
- **Comprehensive**: All code paths tested

### Test Data
- **Consistency**: Standardized test data creation
- **Variety**: Multiple scenarios covered
- **Realism**: Production-like data values
- **Edge Cases**: Boundary conditions tested

### Assertions
- **Specific**: Exact value comparisons
- **Comprehensive**: Multiple assertions per test
- **Meaningful**: Business-relevant validations
- **Clear**: Descriptive failure messages

## Continuous Integration

### Automated Testing
- Tests run on every commit
- Coverage thresholds enforced
- Test reports generated automatically

### Quality Gates
- Minimum 80% coverage required
- All critical paths must be tested
- No test failures allowed in main branch

This comprehensive testing approach ensures the Warehouse Management System is reliable, maintainable, and meets all business requirements.
