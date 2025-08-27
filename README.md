<p align="center">
  <img width=600 src="https://raw.githubusercontent.com/CyR1en/Cardea/refs/heads/master/docs/icons/Cardea_Banner_Transparent.png"/>
</p>

<p align="center">
  <a href="https://modrinth.com/plugin/cardea"><img src="https://img.shields.io/modrinth/v/3C31Qs54?style=for-the-badge&logo=modrinth&logoColor=cad3f5&labelColor=363a4f&color=%23a6da95"></a>
  <a href="https://github.com/CyR1en/Cardea/blob/master/LICENSE"><img src="https://img.shields.io/github/license/cyr1en/Cardea?colorA=363a4f&colorB=91d7e3&style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNTYgMjU2Ij4KPHBhdGggZD0iTTIxNiwzMlYxOTJhOCw4LDAsMCwxLTgsOEg3MmExNiwxNiwwLDAsMC0xNiwxNkgxOTJhOCw4LDAsMCwxLDAsMTZINDhhOCw4LDAsMCwxLTgtOFY1NkEzMiwzMiwwLDAsMSw3MiwyNEgyMDhBOCw4LDAsMCwxLDIxNiwzMloiIHN0eWxlPSJmaWxsOiAjQ0FEM0Y1OyIvPgo8L3N2Zz4=&logoColor=cad3f5"></a>
  <a href="https://discord.com/invite/qHM8kE4XHj"><img src="https://img.shields.io/discord/936346802402238514?style=for-the-badge&color=b7bdf8&labelColor=363a4f&logo=discord&logoColor=cad3f5"></a>
  <a href="https://ko-fi.com/cyr1en"><img src="https://img.shields.io/badge/Kofi-Support_Development-f5a97f?style=for-the-badge&logo=Kofi&logoColor=cad3f5&labelColor=363a4f"></a>
</p>
<h1></h1>

Cardea is an open-source PaperMC plugin written in Kotlin that provides an alternative to whitelisting a server. This is done by implementing a login dialog using the new DialogAPI introduced in Paper 1.21.7.

Features:
- **Easy to setup** - Simply drop it in your plugins folder and set your password.
- **Configurable** - Configure the login dialog to your liking.
- **Modern** - This plugin is written using new features available in [Paper API](https://docs.papermc.io/paper/).
- **Whitelist Alternative** - Users are only required to login once.

<details>
     <summary>Preview</summary>
     <p align="center">
         <img width="600" src="https://github.com/CyR1en/Cardea/blob/master/docs/img.png">
     </p>
</details>
<!-- modrinth_exclude.start -->
## Building

Cardea uses Gradle as a project manager. You can build Cardea for yourself by following the instructions below:

#### Requirements
* JDK 21 or newer
* Git

#### Compiling from source
```sh
git clone https://github.com/CyR1en/Cardea.git
cd Cardea/
./gradlew clean build
```
<!-- modrinth_exclude.end -->
## Special Thanks To:
<div align="Left">
  <a href="https://www.gitbook.com/">
    <img width="230" src="https://i.imgur.com/SIPKmzS.png">
  </a>

  <p>This project owes a huge thanks to GitBook's fantastic <a href="https://docs.gitbook.com/account-management/plans/apply-for-the-non-profit-open-source-plan">Open Source License</a> and their amazing platform for creating beautiful and accessible documentation. Their dedication to open source and ease-of-use has been invaluable to this project's success!</p>

  <a href="https://lithiumhosting.com/">
    <img width="230" src="https://lithiumhosting.com/lithiumv8/images/svg/logo_horizontal_light.svg" />
  </a>

  <p>Lithium Hosting's invaluable support by providing a server to facilitate development. Their dedication to open source and the developer community has been instrumental in making this project possible.</p>
</div>

## License
Cardea is licensed under the permissive [MIT LICENSE](https://github.com/CyR1en/Cardea/blob/master/LICENSE). Please see LICENSE.txt for more info.
