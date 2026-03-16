Android Local Autofill App – MVP Specification
1. Project Overview

Bu proje Android için geliştirilecek bir Autofill uygulamasıdır.

Uygulama kullanıcıdan bazı kişisel bilgileri alır ve bu bilgileri lokal olarak güvenli şekilde saklar. Daha sonra Android’in Autofill Framework sistemi kullanılarak diğer uygulamalardaki form alanlarını otomatik doldurabilir.

Bu sürüm minimum viable product (MVP) olacaktır.

Amaçlar

Android Autofill Service kullanmak

Kullanıcı bilgilerini güvenli saklamak

Form alanlarını algılayıp uygun bilgiyi önermek

Basit bir UI ile kullanıcı verilerini yönetmek

2. Core Features
2.1 User Profile Storage

Uygulama aşağıdaki kullanıcı bilgilerini saklayacaktır.

Full Name
First Name
Last Name
Email
Phone
Address
City
Postal Code
Country

Bu bilgiler

Lokal veritabanında tutulur

Şifrelenmiş olarak saklanır

İnternet bağlantısı gerektirmez

2.2 Secure Storage

Kullanıcı verileri güvenli şekilde saklanmalıdır.

Gereksinimler

Android Keystore kullanılmalı

AES encryption kullanılmalı

Master key Keystore içinde saklanmalı

Teknolojiler

Android Keystore
EncryptedSharedPreferences veya Room + encryption
BiometricPrompt (opsiyonel)
2.3 Autofill Service

Uygulama bir Autofill Service implement edecektir.

Android bileşeni

AutofillService

Service şu görevleri yapacaktır

Android tarafından gönderilen FillRequest'i almak

Form alanlarını analiz etmek

Uygun kullanıcı verisini eşleştirmek

Autofill önerisi üretmek

3. Autofill Logic

Autofill sistemi aşağıdaki bilgileri analiz edecektir

ViewNode.autofillHints
ViewNode.hint
ViewNode.text
ViewNode.idEntry
ViewNode.inputType

Alan tespiti için basit bir mapping kullanılacaktır.

Örnek

email - user.email
phone - user.phone
name - user.fullName
address - user.address

Eğer alan tanınamazsa autofill önerisi gösterilmez.

4. Autofill Flow

Autofill akışı şu şekilde çalışır

User opens a form
      ↓
Android detects autofillable field
      ↓
AutofillService.onFillRequest()
      ↓
Parse AssistStructure
      ↓
Match field types
      ↓
Return FillResponse with Dataset
      ↓
Android shows autofill suggestion
5. Application Architecture

Uygulama aşağıdaki modüllerden oluşacaktır.

app
 ├─ ui
 │   ├─ profile screen
 │   └─ settings screen
 │
 ├─ autofill
 │   ├─ AutofillService
 │   ├─ AutofillParser
 │   └─ AutofillMapper
 │
 ├─ data
 │   ├─ UserProfile
 │   ├─ ProfileRepository
 │   └─ LocalDatabase
 │
 └─ security
     ├─ CryptoManager
     └─ KeyStoreManager
6. Tech Stack

Dil

Kotlin

Android bileşenleri

Jetpack Compose
Room Database
Android Autofill Framework
Android Keystore
ViewModel
Repository pattern

Minimum SDK

Android 8 (API 26)
7. UI Screens
7.1 Profile Setup Screen

Kullanıcı ilk açılışta bilgilerini girer.

Alanlar

Full Name
Email
Phone
Address
City
Postal Code
Country

Kaydet butonu

Save Profile
7.2 Edit Profile Screen

Kullanıcı bilgilerini düzenleyebilir.

7.3 Autofill Activation Screen

Kullanıcıya şu yönerge gösterilir

Enable Autofill Service

Settings → System → Languages & Input → Autofill Service
Select this app
8. Permissions

Manifest içinde gerekli servis tanımı yapılmalıdır.

android.permission.BIND_AUTOFILL_SERVICE

Service

android.service.autofill.AutofillService
9. Autofill Dataset Example

Autofill dataset şu şekilde oluşturulur

Dataset
 ├─ email → user.email
 ├─ phone → user.phone
 ├─ name → user.fullName

Android bu dataset’i suggestion olarak gösterir.

10. Error Handling

Durumlar

kullanıcı profil oluşturmadı

form alanı tanınamadı

autofill devre dışı

Bu durumlarda

No autofill suggestion shown
11. Security Requirements

Bu uygulama hassas veri sakladığı için aşağıdaki kurallar uygulanmalıdır

Tüm veriler şifrelenmiş saklanmalıdır

Master key Android Keystore’da tutulmalıdır

Uygulama internet erişimi gerektirmez

Hiçbir veri dış sunucuya gönderilmez

12. Future Improvements (Not part of MVP)

Gelecek sürümlerde eklenebilir

AI form field detection
MiniLM semantic matching
OCR-based UI parsing
Multiple user profiles
Credit card autofill
Browser extension
13. Expected Deliverables

Antigravity aşağıdaki çıktıları üretmelidir

Working Android project
AutofillService implementation
Profile storage system
Basic Compose UI
Secure local storage