-- Schema creation for Southwest DashPass Application

-- Table: Airport
CREATE TABLE `airport` (
  `airportId` bigint NOT NULL AUTO_INCREMENT,
  `airportCode` varchar(3) NOT NULL,
  `airportName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`airportId`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb3;

-- Table: AuditTrail
CREATE TABLE `AuditTrail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actionType` varchar(255) DEFAULT NULL,
  `customerId` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `employeeId` bigint DEFAULT NULL,
  `entityId` bigint DEFAULT NULL,
  `reservationId` bigint DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb3;

-- Table: Customer
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `can_fly` bit(1) DEFAULT NULL,
  `can_purchase_dash_pass` bit(1) DEFAULT NULL,
  `can_purchase_flight` bit(1) DEFAULT NULL,
  `max_number_of_dashpasses` int DEFAULT NULL,
  `number_of_dash_passes_used` int DEFAULT NULL,
  `total_dash_passes_customer_has` int DEFAULT NULL,
  `total_dash_passes_for_use` int DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `canFly` bit(1) NOT NULL,
  `canPurchaseFlight` bit(1) NOT NULL,
  `available_dash_pass_count` int DEFAULT NULL,
  `can_buy_dash_pass` bit(1) DEFAULT NULL,
  `dash_pass_in_use_count` int DEFAULT NULL,
  `total_dash_pass_count` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj7ja2xvrxudhvssosd4nu1o92` (`user_id`),
  CONSTRAINT `FKra1cb3fu95r1a0m7aksow0nk4` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb3;

-- Table: DashPass
CREATE TABLE `dashPass` (
  `dashPassID` bigint NOT NULL AUTO_INCREMENT,
  `dateOfPurchase` date DEFAULT NULL,
  `expirationDate` date DEFAULT NULL,
  `isRedeemed` bit(1) NOT NULL,
  `customerID` bigint DEFAULT NULL,
  `confirmation_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`dashPassID`),
  KEY `FKcpd31afqbjwl8y5tlokrdvv4r` (`customerID`),
  CONSTRAINT `FKcpd31afqbjwl8y5tlokrdvv4r` FOREIGN KEY (`customerID`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb3;

-- Table: DashPassReservation
CREATE TABLE `dashpass_reservation` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `bookingDate` date DEFAULT NULL,
  `customerID` bigint NOT NULL,
  `dashpassID` bigint NOT NULL,
  `flight_id` bigint DEFAULT NULL,
  `reservation_id` bigint DEFAULT NULL,
  `confirmation_number` varchar(255) DEFAULT NULL,
  `isActive` tinyint(1) DEFAULT '1',
  `isValidated` bit(1) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK1di0wuvptyfkbiedq4b2d2bch` (`dashpassID`),
  KEY `FK63595j4xj6psrs0ifcseggur8` (`customerID`),
  KEY `FK1ia925e0xpprh80ol4nmc83f0` (`flight_id`),
  KEY `FKnvytrq3cuoeoqfl51grnla5k3` (`reservation_id`),
  CONSTRAINT `FK1ia925e0xpprh80ol4nmc83f0` FOREIGN KEY (`flight_id`) REFERENCES `Flight` (`flightID`),
  CONSTRAINT `FK63595j4xj6psrs0ifcseggur8` FOREIGN KEY (`customerID`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKb9vakxdnut3d1p1uesve413dw` FOREIGN KEY (`dashpassID`) REFERENCES `dashPass` (`dashPassID`),
  CONSTRAINT `FKnvytrq3cuoeoqfl51grnla5k3` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`reservationId`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb3;

-- Table: Employee
CREATE TABLE `EMPLOYEE` (
  `can_add_customer` bit(1) DEFAULT NULL,
  `can_add_customer_flight` bit(1) DEFAULT NULL,
  `can_edit_flight_information` bit(1) DEFAULT NULL,
  `can_redeem_dash_pass` bit(1) DEFAULT NULL,
  `can_remove_dash_pass` bit(1) DEFAULT NULL,
  `can_sell_dash_pass` bit(1) DEFAULT NULL,
  `Role` enum('MANAGER','SALES','SUPPORT','BAGGAGE_CLAIM') DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrdnsf2fyk4lhu3ms6t3wed9kv` (`user_id`),
  CONSTRAINT `FK_user_employee` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb3;

-- Table: EmployeeShift
CREATE TABLE `EmployeeShift` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shiftEnd` datetime(6) NOT NULL,
  `shiftStart` datetime(6) NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjouvdnyhwv25bjit03mmpypcg` (`employee_id`),
  CONSTRAINT `FKjouvdnyhwv25bjit03mmpypcg` FOREIGN KEY (`employee_id`) REFERENCES `EMPLOYEE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Table: Flight
CREATE TABLE `Flight` (
  `flightID` bigint NOT NULL AUTO_INCREMENT,
  `arrivalAirportCode` varchar(255) DEFAULT NULL,
  `arrivalDate` date DEFAULT NULL,
  `arrivalTime` time(6) DEFAULT NULL,
  `availableSeats` int DEFAULT NULL,
  `canAddNewDashPass` bit(1) DEFAULT NULL,
  `canUseExistingDashPass` bit(1) DEFAULT NULL,
  `departureAirportCode` varchar(255) DEFAULT NULL,
  `departureDate` date DEFAULT NULL,
  `departureTime` time(6) DEFAULT NULL,
  `flightNumber` varchar(255) DEFAULT NULL,
  `maxNumberOfDashPassesForFlight` int DEFAULT NULL,
  `numberOfDashPassesAvailable` int DEFAULT NULL,
  `numberOfSeatsAvailable` int DEFAULT NULL,
  `price` double DEFAULT NULL,
  `returnDate` date DEFAULT NULL,
  `seatsSold` int DEFAULT NULL,
  `trip_type` enum('ONE_WAY','ROUND_TRIP') DEFAULT NULL,
  `reservation_id` bigint DEFAULT NULL,
  `return_flight_id` bigint DEFAULT NULL,
  `trip_id` varchar(255) DEFAULT NULL,
  `direction` varchar(255) NOT NULL,
  PRIMARY KEY (`flightID`),
  KEY `FK8lguvt1l79b51xebksl9j95ka` (`return_flight_id`),
  KEY `FKt4hqn5v5ykm9qq84j561mnydp` (`reservation_id`),
  CONSTRAINT `FK8lguvt1l79b51xebksl9j95ka` FOREIGN KEY (`return_flight_id`) REFERENCES `Flight` (`flightID`),
  CONSTRAINT `FKt4hqn5v5ykm9qq84j561mnydp` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`reservationId`)
) ENGINE=InnoDB AUTO_INCREMENT=39510 DEFAULT CHARSET=utf8mb3;

-- Table: PaymentDetails
CREATE TABLE `PaymentDetails` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `encrypted_billing_zip` varchar(255) DEFAULT NULL,
  `encrypted_cvv` varchar(255) DEFAULT NULL,
  `encrypted_card_number` varchar(255) DEFAULT NULL,
  `encrypted_expiration_date` varchar(255) DEFAULT NULL,
  `encrypted_name_on_card` varchar(255) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `savePaymentDetails` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1enl9kyxbf2b7w7iilut12p3n` (`customer_id`),
  CONSTRAINT `FK1enl9kyxbf2b7w7iilut12p3n` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb3;

-- Table: PendingCustomers
CREATE TABLE `pending_customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `firstName` varchar(255) DEFAULT NULL,
  `lastName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;

-- Table: Reservation
CREATE TABLE `reservation` (
  `reservationId` bigint NOT NULL AUTO_INCREMENT,
  `airportCode` varchar(255) DEFAULT NULL,
  `dateBooked` date DEFAULT NULL,
  `flight_departure_date` date DEFAULT NULL,
  `total_price` double DEFAULT NULL,
  `trip_type` enum('ONE_WAY','ROUND_TRIP') DEFAULT NULL,
  `customerID` bigint NOT NULL,
  `paymentStatus` enum('FAILED','PAID','PENDING') DEFAULT NULL,
  `status` enum('INVALID','VALID') DEFAULT NULL,
  `confirmation_number` varchar(255) DEFAULT NULL,
  `isValidated` bit(1) DEFAULT NULL,
  `bagCost` double NOT NULL,
  `bagQuantity` int NOT NULL,
  PRIMARY KEY (`reservationId`),
  KEY `FK608ikihfy2cc0ujc6ukd5j3tj` (`customerID`),
  CONSTRAINT `FK608ikihfy2cc0ujc6ukd5j3tj` FOREIGN KEY (`customerID`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb3;

-- Table: Reservation_flightGates
CREATE TABLE `Reservation_flightGates` (
  `Reservation_reservationId` bigint NOT NULL,
  `flightGates` varchar(255) DEFAULT NULL,
  `flightGates_KEY` bigint NOT NULL,
  PRIMARY KEY (`Reservation_reservationId`,`flightGates_KEY`),
  CONSTRAINT `FKfa7csuxpjox8ythdp9gjotc02` FOREIGN KEY (`Reservation_reservationId`) REFERENCES `reservation` (`reservationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Table: Reservation_flightTerminals
CREATE TABLE `Reservation_flightTerminals` (
  `Reservation_reservationId` bigint NOT NULL,
  `flightTerminals` varchar(255) DEFAULT NULL,
  `flightTerminals_KEY` bigint NOT NULL,
  PRIMARY KEY (`Reservation_reservationId`,`flightTerminals_KEY`),
  CONSTRAINT `FKt8urv9nq3fv1x8p62vb67wp6i` FOREIGN KEY (`Reservation_reservationId`) REFERENCES `reservation` (`reservationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Table: SalesRecord
CREATE TABLE `SalesRecord` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `saleDate` date DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `dash_pass_id` bigint DEFAULT NULL,
  `employee_id` bigint DEFAULT NULL,
  `flight_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs6tdtvrxkq9mu501eh94ek3y` (`customer_id`),
  KEY `FK4ys4n8hr54y5ymh17wt802fj4` (`dash_pass_id`),
  KEY `FK7ce9dskk03a0f0j1n0n9fbu93` (`employee_id`),
  KEY `FKfsk6xp5kr7nsfxeani7d2vi61` (`flight_id`),
  CONSTRAINT `FK4ys4n8hr54y5ymh17wt802fj4` FOREIGN KEY (`dash_pass_id`) REFERENCES `dashPass` (`dashPassID`),
  CONSTRAINT `FK7ce9dskk03a0f0j1n0n9fbu93` FOREIGN KEY (`employee_id`) REFERENCES `EMPLOYEE` (`id`),
  CONSTRAINT `FKfsk6xp5kr7nsfxeani7d2vi61` FOREIGN KEY (`flight_id`) REFERENCES `Flight` (`flightID`),
  CONSTRAINT `FKs6tdtvrxkq9mu501eh94ek3y` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb3;

-- Table: Shift
CREATE TABLE `Shift` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` datetime(6) DEFAULT NULL,
  `endTime` datetime(6) DEFAULT NULL,
  `startTime` datetime(6) DEFAULT NULL,
  `employee_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlkk5jpvw336jap3xqv7y87p3k` (`employee_id`),
  CONSTRAINT `FKlkk5jpvw336jap3xqv7y87p3k` FOREIGN KEY (`employee_id`) REFERENCES `EMPLOYEE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=327 DEFAULT CHARSET=utf8mb3;

-- Table: SupportRequest
CREATE TABLE `SupportRequest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createdDate` datetime(6) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `priority` enum('NORMAL','URGENT') DEFAULT NULL,
  `status` enum('CLOSED','ESCALATED','IN_PROGRESS','OPEN') DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `employee_id` bigint DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp7u1vyv9tq77bv9p00ayopxy0` (`employee_id`),
  KEY `FKaupim6h58aakkuofe93ueokvg` (`customer_id`),
  CONSTRAINT `FKaupim6h58aakkuofe93ueokvg` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKp7u1vyv9tq77bv9p00ayopxy0` FOREIGN KEY (`employee_id`) REFERENCES `EMPLOYEE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb3;

-- Table: TimeOffRequest
CREATE TABLE `TimeOffRequest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `endDate` date NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `startDate` date NOT NULL,
  `status` enum('APPROVED','DECLINED','PENDING') NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKendb7bqhljo1rncinu2tahqr` (`employee_id`),
  CONSTRAINT `FKendb7bqhljo1rncinu2tahqr` FOREIGN KEY (`employee_id`) REFERENCES `EMPLOYEE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Table: Users
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `firstName` varchar(255) NOT NULL,
  `lastName` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `userType` enum('CUSTOMER','EMPLOYEE') NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb3;
