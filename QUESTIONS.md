# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor several aspects of the database access layer for consistency and maintainability:

1. **Standardize Repository Pattern**: The codebase mixes PanacheRepository (Warehouse) with direct entity methods (Store, Product). I would standardize all entities to use the same repository pattern for consistency.

2. **Introduce Service Layer**: Currently, REST endpoints directly call use cases/repositories. I'd add a service layer to better separate concerns and make the code more testable.

3. **Consistent Transaction Management**: The StoreResource uses @Transactional directly on endpoints, while Warehouse operations handle transactions through use cases. I'd centralize transaction management.

4. **Database Configuration**: Extract database-specific configurations to a more flexible configuration system to support different environments.

5. **Error Handling**: Implement consistent exception handling across all database operations to provide better error responses.

These refactors would improve code maintainability, testability, and consistency across the application.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
**OpenAPI Code Generation (Warehouse approach):**
Pros:
- Type safety and compile-time validation
- Automatic documentation generation
- Consistent API contract enforcement
- Reduced boilerplate code
- Easy client SDK generation
- Standardized error handling

Cons:
- Less flexibility for custom logic
- Generated code can be verbose
- Harder to customize certain behaviors
- Learning curve for code generation tools
- Dependency on external tools

**Manual Coding (Product/Store approach):**
Pros:
- Full control over implementation
- Easier to customize behavior
- No external dependencies
- Simpler for simple APIs
- More intuitive for developers

Cons:
- Risk of inconsistencies
- Manual documentation maintenance
- More boilerplate code
- Higher chance of human error
- No automatic contract validation

**My Choice:**
I would choose **OpenAPI code generation** for this project because:
1. It's a warehouse management system where API consistency is crucial
2. Multiple teams/consumers will benefit from standardized contracts
3. The business logic is complex enough that type safety provides real value
4. Long-term maintenance benefits outweigh the initial learning curve

However, I'd use a hybrid approach: OpenAPI for core business entities (Warehouse, Product, Store) and manual coding for utility/admin endpoints.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
**Testing Priority Strategy:**

1. **Unit Tests (Highest Priority) - 70% effort:**
   - Focus on business logic in use cases (CreateWarehouseUseCase, ReplaceWarehouseUseCase)
   - Test all validation rules and edge cases
   - Mock external dependencies (LocationResolver, WarehouseStore)
   - Quick feedback loop, catches most bugs early

2. **Integration Tests (Medium Priority) - 20% effort:**
   - Test database operations with real repositories
   - Verify transaction management works correctly
   - Test REST endpoints with in-memory database (H2)
   - Ensure different components work together

3. **End-to-End Tests (Low Priority) - 10% effort:**
   - Critical user journeys only (warehouse creation, replacement)
   - Use TestContainers for real database testing
   - Focus on integration with external systems (legacy gateway)

**Implementation Approach:**

1. **Test Coverage Monitoring:**
   - Set JaCoCo minimum coverage thresholds (80% for new code)
   - Focus on covering business logic, not getters/setters
   - Use coverage reports to identify gaps weekly

2. **Automated Testing Pipeline:**
   - Unit tests run on every commit
   - Integration tests run on PRs
   - E2E tests run nightly

3. **Maintaining Effectiveness Over Time:**
   - Review and update tests when business rules change
   - Use parameterized tests for validation scenarios
   - Implement test data factories for consistent test data
   - Regularly refactor tests to prevent test decay

4. **Resource-Constrained Strategy:**
   - Start with critical path testing (warehouse operations)
   - Add regression tests for bug fixes
   - Use risk-based approach: test high-impact features first
   - Leverage existing test infrastructure (Quarkus Test DevTools)

This approach ensures comprehensive testing of business logic while respecting time constraints and maintaining long-term code quality.
```