-- ============================================================================
-- V5__insert_skills_data.sql
-- Migration: Insert initial skill data
-- Description: Populates the skills table with common technical skills
-- Date: 2026-01-05
-- ============================================================================

-- Insert skills data
-- Note: Using INSERT ... ON CONFLICT DO NOTHING to make migration idempotent

-- ============================================================================
-- BACKEND SKILLS
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'Java', 'Object-oriented programming language, widely used for enterprise applications', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Spring Boot', 'Java framework for building microservices and web applications', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Spring Framework', 'Comprehensive framework for enterprise Java development', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Node.js', 'JavaScript runtime for building scalable server-side applications', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Express.js', 'Fast, minimalist web framework for Node.js', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Python', 'High-level programming language for web development, data science, and automation', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Django', 'High-level Python web framework', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Flask', 'Lightweight Python web framework', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'C#', 'Modern object-oriented programming language for .NET applications', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), '.NET Core', 'Cross-platform framework for building modern applications', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'PHP', 'Server-side scripting language for web development', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Laravel', 'PHP framework for web artisans', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Ruby on Rails', 'Web application framework written in Ruby', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Go', 'Statically typed, compiled programming language by Google', 'Backend', NOW(), NOW(), false),
(gen_random_uuid(), 'Rust', 'Systems programming language focused on safety and performance', 'Backend', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- FRONTEND SKILLS
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'JavaScript', 'Programming language for web interactivity', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'TypeScript', 'Typed superset of JavaScript', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'React', 'JavaScript library for building user interfaces', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Vue.js', 'Progressive JavaScript framework', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Angular', 'Platform for building web applications', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Next.js', 'React framework for production', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Nuxt.js', 'Vue.js framework for universal applications', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'HTML5', 'Markup language for web pages', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'CSS3', 'Style sheet language for web design', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Sass/SCSS', 'CSS preprocessor for advanced styling', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Tailwind CSS', 'Utility-first CSS framework', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Bootstrap', 'Popular CSS framework for responsive design', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Material-UI', 'React component library implementing Material Design', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Redux', 'State management library for JavaScript apps', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Webpack', 'Module bundler for JavaScript applications', 'Frontend', NOW(), NOW(), false),
(gen_random_uuid(), 'Vite', 'Next generation frontend tooling', 'Frontend', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- MOBILE DEVELOPMENT
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'React Native', 'Framework for building native mobile apps using React', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'Flutter', 'Google UI toolkit for building natively compiled applications', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'Swift', 'Programming language for iOS development', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'Kotlin', 'Modern programming language for Android development', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'Android SDK', 'Software development kit for Android applications', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'iOS SDK', 'Software development kit for iOS applications', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'Ionic', 'Cross-platform mobile app development framework', 'Mobile', NOW(), NOW(), false),
(gen_random_uuid(), 'Xamarin', 'Microsoft framework for cross-platform mobile development', 'Mobile', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- DATABASE SKILLS
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'PostgreSQL', 'Advanced open-source relational database', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'MySQL', 'Popular open-source relational database', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'MongoDB', 'NoSQL document-oriented database', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'Redis', 'In-memory data structure store, cache, and message broker', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'Oracle Database', 'Enterprise relational database management system', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'Microsoft SQL Server', 'Relational database management system by Microsoft', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'SQLite', 'Lightweight embedded database', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'Elasticsearch', 'Distributed search and analytics engine', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'Cassandra', 'Distributed NoSQL database for high availability', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'DynamoDB', 'Amazon fully managed NoSQL database', 'Database', NOW(), NOW(), false),
(gen_random_uuid(), 'Firebase', 'Google platform for mobile and web applications', 'Database', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- DEVOPS & CLOUD SKILLS
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'Docker', 'Platform for developing, shipping, and running applications in containers', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Kubernetes', 'Container orchestration platform', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Jenkins', 'Automation server for continuous integration and delivery', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'GitLab CI/CD', 'Continuous integration and deployment with GitLab', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'GitHub Actions', 'CI/CD platform integrated with GitHub', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'AWS', 'Amazon Web Services cloud platform', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Azure', 'Microsoft cloud computing platform', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Google Cloud Platform', 'Google cloud computing services', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Terraform', 'Infrastructure as code software tool', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Ansible', 'Automation tool for configuration management', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Linux/Unix', 'Operating system administration and shell scripting', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Nginx', 'High-performance web server and reverse proxy', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Apache', 'Open-source web server software', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Prometheus', 'Monitoring and alerting toolkit', 'DevOps', NOW(), NOW(), false),
(gen_random_uuid(), 'Grafana', 'Analytics and monitoring platform', 'DevOps', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- TESTING SKILLS
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'JUnit', 'Unit testing framework for Java', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Mockito', 'Mocking framework for unit tests in Java', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Jest', 'JavaScript testing framework', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Mocha', 'JavaScript test framework', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Cypress', 'End-to-end testing framework', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Selenium', 'Browser automation tool for testing', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Postman', 'API development and testing tool', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'JMeter', 'Performance testing tool', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'Pytest', 'Testing framework for Python', 'Testing', NOW(), NOW(), false),
(gen_random_uuid(), 'TestNG', 'Testing framework inspired by JUnit', 'Testing', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- OTHER SKILLS
-- ============================================================================
INSERT INTO skills (id, name, description, category, created_at, updated_at, delete_flag) VALUES
(gen_random_uuid(), 'Git', 'Distributed version control system', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'GitHub', 'Web-based Git repository hosting service', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'GitLab', 'DevOps platform with Git repository management', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Bitbucket', 'Git-based code hosting and collaboration tool', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'REST API', 'Architectural style for web services', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'GraphQL', 'Query language for APIs', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Microservices', 'Architectural style for building applications', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Agile/Scrum', 'Agile project management methodology', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'JIRA', 'Project management and issue tracking tool', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'OAuth 2.0', 'Authorization framework for secure API access', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'JWT', 'JSON Web Token for secure information transmission', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'WebSocket', 'Protocol for real-time bidirectional communication', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'RabbitMQ', 'Message broker software', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Apache Kafka', 'Distributed event streaming platform', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Machine Learning', 'Artificial intelligence and data analysis', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'TensorFlow', 'Machine learning framework', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'PyTorch', 'Machine learning library', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Data Science', 'Extracting insights from structured and unstructured data', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Blockchain', 'Distributed ledger technology', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'UI/UX Design', 'User interface and user experience design', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Figma', 'Collaborative design tool', 'Other', NOW(), NOW(), false),
(gen_random_uuid(), 'Adobe XD', 'Vector-based user experience design tool', 'Other', NOW(), NOW(), false)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- Total skills inserted:
-- - Backend: 15 skills
-- - Frontend: 16 skills
-- - Mobile: 8 skills
-- - Database: 11 skills
-- - DevOps: 15 skills
-- - Testing: 10 skills
-- - Other: 22 skills
-- Total: 97 skills

COMMIT;
