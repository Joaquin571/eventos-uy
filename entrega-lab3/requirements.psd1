@{
    SchemaVersion = 1
    Architecture = 'x64'
    Roles = @{
        Central = @('Java', 'PostgreSQL')
        Webapp  = @('Java', 'Tomcat')
    }
    Java = @{
        Major = 25
        WingetId = 'EclipseAdoptium.Temurin.25.JDK'
        Source = 'winget'
    }
    PostgreSQL = @{
        Major = 17
        WingetId = 'PostgreSQL.PostgreSQL.17'
        Source = 'winget'
    }
    Tomcat = @{
        Major = 11
        Version = '11.0.29'
        Url = 'https://downloads.apache.org/tomcat/tomcat-11/v11.0.29/bin/apache-tomcat-11.0.29-windows-x64.zip'
        Sha512 = '8df8d90f4b8cd3e7e5c0c548fab93f99d6acce1ffd57e68ea5fa4840a02da91cb2970ca3d4c30f6c0253aaceb59dde6b85c3ba358478c67201867907ae37d73b'
        DirectoryName = 'apache-tomcat-11.0.29'
    }
    Maven = @{
        RequiredOnExecution = $false
    }
}
