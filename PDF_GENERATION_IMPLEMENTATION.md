# PDF Generation Implementation for Smart_rdv

## Overview
This implementation adds PDF generation functionality for appointment confirmations with QR code encoding.

## Features
- Generates professional PDF confirmations for appointments
- Includes patient name, appointment date/time, doctor information, and status
- Contains a QR code that encodes all appointment details as text
- Secure endpoint that only allows users to access their own appointment PDFs

## Implementation Details

### Dependencies Added
- **iText 5.5.13.3**: PDF generation library
- **ZXing 3.5.2**: QR code generation library (core and javase modules)

### Files Created/Modified

#### 1. `pom.xml`
Added dependencies for iText and ZXing libraries.

#### 2. `PdfService.java` (New)
- Service class that handles PDF generation
- Method: `generateAppointmentPdf(RendezVous rendezVous)` returns `byte[]`
- Creates formatted PDF with appointment details table
- Generates QR code containing appointment information
- Handles null values gracefully

#### 3. `ReservationController.java` (Modified)
- Added `PdfService` dependency injection
- New endpoint: `GET /reservations/{id}/pdf`
- Security: Only allows users to access their own appointment PDFs
- Returns PDF as downloadable file with proper headers

#### 4. `PdfServiceTest.java` (New)
- Unit tests for PDF generation functionality
- Tests both normal and edge cases (null values)
- Verifies PDF generation produces valid output

## API Endpoint

### GET `/reservations/{id}/pdf`
- **Purpose**: Download appointment confirmation PDF
- **Authentication**: Required (JWT token)
- **Authorization**: User can only access their own appointments
- **Response**: PDF file as downloadable attachment
- **Filename**: `appointment_{id}_{date}.pdf`

## PDF Contents

### Appointment Details Table
- Patient Name
- Appointment Date (formatted as "MMMM dd, yyyy")
- Appointment Time (formatted as "hh:mm a")
- Doctor Name (currently set to "Dr. Medical Center")
- Status

### QR Code
- Contains all appointment details as text
- Size: 150x150 pixels
- Scannable with any QR code reader

### Additional Elements
- Professional title header
- Footer with arrival instructions
- Clean, professional layout

## Usage Example

```bash
# Get appointment PDF (requires authentication)
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/reservations/123/pdf \
     --output appointment_confirmation.pdf
```

## Security Considerations
- Users can only access PDFs for their own appointments
- Proper authentication required via JWT token
- Access control prevents unauthorized data access

## Future Enhancements
- Add actual doctor name from database when doctor entity is available
- Add clinic logo and branding
- Include appointment notes in PDF
- Add multiple language support
- Email PDF automatically upon booking confirmation

## Testing
- Unit tests verify PDF generation works correctly
- Tests handle edge cases like null values
- Compilation and tests pass successfully
