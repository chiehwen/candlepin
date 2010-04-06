Name: candlepin
Summary: Candlepin is an open source entitlement management system.
Group: Internet/Applications
License: GLPv2
Version: 1.0.1
Release: 1
URL: http://fedorahosted.org/candlepin
# Source0: https://fedorahosted.org/releases/c/a/candlepin/%{name}-%{version}.tar.gz
Source: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Vendor: Red Hat, Inc
BuildArch: noarch

Requires: candlepin-webapp
BuildRequires: java >= 0:1.6.0
BuildRequires: rubygem-buildr
%define __jar_repack %{nil}

%description
Candlepin is an open source entitlement management system.

%package tomcat5
Summary: Candlepin web application for tomcat5
Requires: tomcat5 >= 5.5
Provides: candlepin-webapp

%description tomcat5
Candlepin web application for tomcat5

%package tomcat6
Summary: Candlepin web application for tomcat6
Requires: tomcat6
Provides: candlepin-webapp

%description tomcat6
Candlepin web application for tomcat6

#%package jboss
#Summary: Candlepin web application for jboss
#Provides: candlepin-webapp

#%description jboss
#Candlepin web application for jboss

%prep
%setup -q 

%build
buildr clean test=no package

%install
rm -rf $RPM_BUILD_ROOT
# Create the directory structure required to lay down our files
# tomcat5
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat5/webapps/
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat5/webapps/candlepin/

# tomcat6
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat6/webapps/
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat6/webapps/candlepin/

# need to get rpm and war version in sync
unzip target/%{name}-*.war -d $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat5/webapps/candlepin/
unzip target/%{name}-*.war -d $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat6/webapps/candlepin/

%clean
rm -rf $RPM_BUILD_ROOT

#%files
#%defattr(-,jboss,jboss,-)
#/var/lib/tomcat5/webapps/candlepin-1.0.0.war

%files tomcat5
%defattr(644,tomcat,tomcat,775)
%{_localstatedir}/lib/tomcat5/webapps/candlepin*

%files tomcat6
%defattr(644,tomcat,tomcat,775)
%{_localstatedir}/lib/tomcat6/webapps/candlepin*

%doc
