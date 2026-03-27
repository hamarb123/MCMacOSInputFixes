using System.IO.Compression;
using System.Text;

var dir = Environment.CurrentDirectory;
var iconFile = Path.Combine(dir, "src", "main", "resources", "assets", "macos_input_fixes", "icon.png");
var jsonFile = File.ReadAllText(Path.Combine(dir, "util", "helper.mod.json"));
var optionsFile = File.ReadAllText(Path.Combine(dir, "gradle.properties"));
string? modVersion = null;
foreach (var line in optionsFile.EnumerateLines())
{
	if (line.StartsWith('#')) continue;
	if (line.Trim().Length == 0) continue;
	var eqIdx = line.IndexOf('=');
	if (eqIdx < 0) continue;
	var key = line[..eqIdx].Trim();
	var value = line[(eqIdx + 1)..].Trim();
	jsonFile = jsonFile.Replace($"${{{key}}}", value.ToString());
	if (key.SequenceEqual("mod_version")) modVersion = value.ToString();
}
if (modVersion == null) throw new Exception("mod_version not found in gradle.properties");
string[] mcVersions = ["1.21.11", "26.1"];
using var output = File.Open(Path.Combine(dir, "build", "libs", $"macos-input-fixes-{modVersion}.jar"), FileMode.Create);
using (var zip = new ZipArchive(output, ZipArchiveMode.Create, leaveOpen: true))
{
	// Add fabric.mod.json
	{
		var modJsonEntry = zip.CreateEntry("fabric.mod.json");
		using var modJsonStream = modJsonEntry.Open();
		using var writer = new StreamWriter(modJsonStream, Encoding.UTF8);
		writer.Write(jsonFile);
	}

	// Add icon
	{
		var iconEntry = zip.CreateEntry("assets/macos_input_fixes/icon.png");
		using var iconStream = iconEntry.Open();
		using var fileStream = File.OpenRead(iconFile);
		fileStream.CopyTo(iconStream);
	}

	// Add jars
	{
		foreach (var mcVersion in mcVersions)
		{
			var jarEntry = zip.CreateEntry($"META-INF/jars/macos-input-fixes-{modVersion}+{mcVersion}.jar");
			using var jarStream = jarEntry.Open();
			using var fileStream = File.OpenRead(Path.Combine(dir, "build", "libs", modVersion, $"macos-input-fixes-{modVersion}+{mcVersion}.jar"));
			fileStream.CopyTo(jarStream);
		}
	}
}
Console.WriteLine("Successfully packaged mod!");
