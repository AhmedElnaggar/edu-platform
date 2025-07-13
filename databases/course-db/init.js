// Course Service MongoDB initialization script
// This script sets up the course database with collections, indexes, and sample data

// Switch to course database
db = db.getSiblingDB('course_service');

// Drop existing collections if they exist (for fresh setup)
db.courses.drop();
db.enrollments.drop();
db.course_modules.drop();
db.course_lessons.drop();
db.course_reviews.drop();
db.course_analytics.drop();
db.course_categories.drop();

// Create collections
db.createCollection('courses');
db.createCollection('enrollments');
db.createCollection('course_modules');
db.createCollection('course_lessons');
db.createCollection('course_reviews');
db.createCollection('course_analytics');
db.createCollection('course_categories');

// Create indexes for courses collection
db.courses.createIndex({ "instructorId": 1 });
db.courses.createIndex({ "category": 1 });
db.courses.createIndex({ "active": 1 });
db.courses.createIndex({ "title": "text", "description": "text" });
db.courses.createIndex({ "tags": 1 });
db.courses.createIndex({ "difficulty": 1 });
db.courses.createIndex({ "price": 1 });
db.courses.createIndex({ "rating": -1 });
db.courses.createIndex({ "createdAt": -1 });
db.courses.createIndex({ "publishedAt": -1 });
db.courses.createIndex({ "slug": 1 }, { unique: true });

// Create indexes for enrollments collection
db.enrollments.createIndex({ "userId": 1 });
db.enrollments.createIndex({ "courseId": 1 });
db.enrollments.createIndex({ "userId": 1, "courseId": 1 }, { unique: true });
db.enrollments.createIndex({ "status": 1 });
db.enrollments.createIndex({ "enrolledAt": -1 });
db.enrollments.createIndex({ "completedAt": -1 });

// Create indexes for course_modules collection
db.course_modules.createIndex({ "courseId": 1 });
db.course_modules.createIndex({ "orderIndex": 1 });
db.course_modules.createIndex({ "courseId": 1, "orderIndex": 1 });

// Create indexes for course_lessons collection
db.course_lessons.createIndex({ "courseId": 1 });
db.course_lessons.createIndex({ "moduleId": 1 });
db.course_lessons.createIndex({ "courseId": 1, "moduleId": 1, "orderIndex": 1 });

// Create indexes for course_reviews collection
db.course_reviews.createIndex({ "courseId": 1 });
db.course_reviews.createIndex({ "userId": 1 });
db.course_reviews.createIndex({ "rating": -1 });
db.course_reviews.createIndex({ "createdAt": -1 });

// Create indexes for course_analytics collection
db.course_analytics.createIndex({ "courseId": 1 });
db.course_analytics.createIndex({ "date": -1 });
db.course_analytics.createIndex({ "courseId": 1, "date": -1 });

// Create indexes for course_categories collection
db.course_categories.createIndex({ "name": 1 }, { unique: true });
db.course_categories.createIndex({ "parentId": 1 });
db.course_categories.createIndex({ "slug": 1 }, { unique: true });

// Insert course categories
db.course_categories.insertMany([
    {
        _id: ObjectId("650000000000000000000001"),
        name: "Programming",
        slug: "programming",
        description: "Programming and software development courses",
        parentId: null,
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000002"),
        name: "Web Development",
        slug: "web-development",
        description: "Frontend and backend web development",
        parentId: ObjectId("650000000000000000000001"),
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000003"),
        name: "Mobile Development",
        slug: "mobile-development",
        description: "iOS and Android app development",
        parentId: ObjectId("650000000000000000000001"),
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000004"),
        name: "Data Science",
        slug: "data-science",
        description: "Data analysis, machine learning, and AI",
        parentId: null,
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000005"),
        name: "Business",
        slug: "business",
        description: "Business and management courses",
        parentId: null,
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

// Insert sample courses
db.courses.insertMany([
    {
        _id: ObjectId("650000000000000000000101"),
        title: "Complete Spring Boot Masterclass",
        slug: "complete-spring-boot-masterclass",
        description: "Learn Spring Boot from scratch and build enterprise-level applications",
        shortDescription: "Master Spring Boot with hands-on projects and real-world examples",
        instructorId: "instructor1",
        category: "Web Development",
        categoryId: ObjectId("650000000000000000000002"),
        difficulty: "INTERMEDIATE",
        price: 99.99,
        currency: "USD",
        discountPrice: 79.99,
        discountExpiry: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days from now
        duration: 1200, // minutes
        maxStudents: 500,
        currentEnrollments: 0,
        rating: 4.5,
        reviewCount: 0,
        tags: ["spring", "java", "backend", "microservices", "api"],
        requirements: [
            "Basic knowledge of Java programming",
            "Understanding of object-oriented programming",
            "Familiarity with web development concepts"
        ],
        outcomes: [
            "Build Spring Boot applications from scratch",
            "Implement REST APIs with Spring Boot",
            "Work with databases using Spring Data JPA",
            "Deploy applications to cloud platforms",
            "Implement security with Spring Security"
        ],
        language: "English",
        subtitles: ["English", "Spanish", "Arabic"],
        thumbnailUrl: "https://example.com/thumbnails/spring-boot.jpg",
        previewVideoUrl: "https://example.com/previews/spring-boot.mp4",
        status: "PUBLISHED",
        active: true,
        createdAt: new Date(),
        updatedAt: new Date(),
        publishedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000102"),
        title: "React.js Complete Guide",
        slug: "react-js-complete-guide",
        description: "Master React.js and build modern web applications with hooks, context, and more",
        shortDescription: "Learn React.js with practical examples and modern patterns",
        instructorId: "instructor2",
        category: "Web Development",
        categoryId: ObjectId("650000000000000000000002"),
        difficulty: "BEGINNER",
        price: 79.99,
        currency: "USD",
        discountPrice: null,
        discountExpiry: null,
        duration: 900, // minutes
        maxStudents: 300,
        currentEnrollments: 0,
        rating: 4.7,
        reviewCount: 0,
        tags: ["react", "javascript", "frontend", "hooks", "jsx"],
        requirements: [
            "Basic HTML, CSS, and JavaScript knowledge",
            "Understanding of ES6 features",
            "Node.js installed on your machine"
        ],
        outcomes: [
            "Build React applications from scratch",
            "Use React hooks effectively",
            "Manage state with Context API",
            "Create reusable components",
            "Deploy React applications"
        ],
        language: "English",
        subtitles: ["English", "French", "German"],
        thumbnailUrl: "https://example.com/thumbnails/react.jpg",
        previewVideoUrl: "https://example.com/previews/react.mp4",
        status: "PUBLISHED",
        active: true,
        createdAt: new Date(),
        updatedAt: new Date(),
        publishedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000103"),
        title: "Python for Data Science",
        slug: "python-for-data-science",
        description: "Learn Python programming for data analysis, visualization, and machine learning",
        shortDescription: "Master Python for data science with pandas, numpy, and scikit-learn",
        instructorId: "instructor3",
        category: "Data Science",
        categoryId: ObjectId("650000000000000000000004"),
        difficulty: "INTERMEDIATE",
        price: 119.99,
        currency: "USD",
        discountPrice: 89.99,
        discountExpiry: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000), // 15 days from now
        duration: 1500, // minutes
        maxStudents: 200,
        currentEnrollments: 0,
        rating: 4.8,
        reviewCount: 0,
        tags: ["python", "data-science", "pandas", "numpy", "machine-learning"],
        requirements: [
            "Basic programming knowledge",
            "Understanding of mathematics and statistics",
            "Python 3.7+ installed"
        ],
        outcomes: [
            "Manipulate data with pandas",
            "Create visualizations with matplotlib and seaborn",
            "Build machine learning models",
            "Work with real datasets",
            "Deploy ML models to
            ] ,
        language: "English",
        subtitles: ["English", "French", "German"],
        thumbnailUrl: "https://example.com/thumbnails/react.jpg",
        previewVideoUrl: "https://example.com/previews/react.mp4",
        status: "PUBLISHED",
        active: true,
        createdAt: new Date(),
        updatedAt: new Date(),
        publishedAt: new Date()
                }
]);

