/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.scorer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.cmu.meteor.util.Constants;

/**
 * Meteor configuration class which can be instantiated, modified with set()
 * methods, and passed to a new MeteorScorer
 * 
 * Important: setting language also sets default task parameters for that
 * language. Task sets parameters and weights. Make sure to set options in the
 * following order:
 * 
 * 1. language
 * 
 * 2. task
 * 
 * 3. everything else
 */
public class MeteorConfiguration {

	/* Configuration Instance */

	private String language;
	private int langID;
	private String task;
	private int normalization;
	private ArrayList<Double> parameters;
	private ArrayList<Integer> modules;
	private ArrayList<Double> moduleWeights;
	private int beamSize;
	private URL wordFileURL;
	private URL synDirURL;
	private URL paraDirURL;
	private boolean charBased;

	/**
	 * Create configuration with default parameters
	 */
	public MeteorConfiguration() {
		setDefaults();
	}

	private void setDefaults() {
		setLanguage("english");
		setTask("default");
		setBeamSize(Constants.DEFAULT_BEAM_SIZE);
		setWordFileURL(Constants.getDefaultWordFileURL(langID));
		setSynDirURL(Constants.DEFAULT_SYN_DIR_URL);
		setParaFileURL(Constants.getDefaultParaFileURL(langID));
		setNormalization(Constants.NO_NORMALIZE);
		setCharBased(false);
	}

	/**
	 * Return the string identifying this Meteor scorer configuration.
	 */
	public String getConfigID() {
		// Format: meteor-version-ch-lang-norm-params-mods-weights
		return "meteor-" + Constants.VERSION + "-"
				+ (this.charBased ? "ch-" : "wo-")
				+ Constants.getLanguageShortName(this.langID) + "-"
				+ Constants.getNormName(this.normalization) + "-"
				+ Constants.getWeightListString(this.parameters) + "-"
				+ Constants.getModuleListString(this.modules) + "-"
				+ Constants.getWeightListString(this.moduleWeights);
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = Constants.normLanguageName(language);
		this.langID = Constants.getLanguageID(this.language);
		setParaFileURL(Constants.getDefaultParaFileURL(langID));
		int defaultTask = Constants.getDefaultTask(langID);
		setTask(defaultTask);

	}

	// No setter for langID since it must correspond to language

	public int getLangID() {
		return langID;
	}

	public String getTask() {
		return task;
	}

	public String getTaskDesc() {
		return Constants.getTaskDescription(task);
	}

	/**
	 * Sets task by name plus default parameters, modules, and module weights
	 * for task.
	 * 
	 * @param task
	 */
	public void setTask(String task) {
		setTask(Constants.getTaskID(task));
	}

	public void setTask(int taskID) {
		setParameters(Constants.getParameters(langID, taskID));
		setModules(Constants.getModules(langID, taskID));
		ArrayList<Double> weightList = Constants.getModuleWeights(langID,
				taskID);
		ArrayList<Double> sizedWeightList = new ArrayList<Double>();
		for (int i = 0; i < modules.size(); i++)
			sizedWeightList.add(weightList.get(i));
		setModuleWeights(sizedWeightList);
		setTaskName(Constants.getTaskName(taskID));
		// Utility task uses character-based evaluation
		setCharBased(taskID == Constants.TASK_UTIL);
	}

	// Only used by other methods
	private void setTaskName(String name) {
		this.task = name;
	}

	public int getNormalization() {
		return normalization;
	}

	public void setNormalization(int normalization) {
		this.normalization = normalization;
	}

	public ArrayList<Double> getParameters() {
		return new ArrayList<Double>(parameters);
	}

	public String getParametersString() {
		StringBuilder sb = new StringBuilder();
		for (Double param : parameters)
			sb.append(param + " ");
		return sb.toString().trim();
	}

	public void setParameters(ArrayList<Double> parameters) {
		setTaskName("custom");
		this.parameters = new ArrayList<Double>(parameters);
	}

	public void setParameters(String language, String taskName) {
		setTaskName(taskName);
		parameters = Constants.getParameters(
				Constants.getLanguageID(Constants.normLanguageName(language)),
				Constants.getTaskID(taskName));
	}

	public ArrayList<Integer> getModules() {
		return new ArrayList<Integer>(modules);
	}

	public String getModulesString() {
		StringBuilder sb = new StringBuilder();
		for (Integer module : modules)
			sb.append(Constants.getModuleName(module) + " ");
		return sb.toString().trim();
	}

	public void setModules(ArrayList<Integer> modules) {
		setTaskName("custom");
		this.modules = new ArrayList<Integer>();
		for (Integer modID : modules)
			this.modules.add(modID);
	}

	public void setModulesByName(ArrayList<String> modules) {
		setTaskName("custom");
		this.modules = new ArrayList<Integer>();
		for (String modName : modules)
			this.modules.add(Constants.getModuleID(modName));
	}

	public ArrayList<Double> getModuleWeights() {
		return new ArrayList<Double>(moduleWeights);
	}

	public String getModuleWeightsString() {
		StringBuilder sb = new StringBuilder();
		for (Double weight : moduleWeights)
			sb.append(weight + " ");
		return sb.toString().trim();
	}

	public void setModuleWeights(ArrayList<Double> moduleScores) {
		setTaskName("custom");
		this.moduleWeights = new ArrayList<Double>(moduleScores);
	}

	public URL getWordFileURL() {
		return wordFileURL;
	}

	public void setWordFileURL(URL wordFileURL) {
		try {
			// This should not ever throw a malformed url exception
			this.wordFileURL = new URL(wordFileURL.toString());
		} catch (MalformedURLException ex) {
			System.err.println("Error: Word list directory URL NOT set");
			ex.printStackTrace();
		}
	}

	public URL getSynDirURL() {
		return synDirURL;
	}

	public void setSynDirURL(URL synDirURL) {
		try {
			// This should not ever throw a malformed url exception
			this.synDirURL = new URL(synDirURL.toString());
		} catch (MalformedURLException ex) {
			System.err.println("Error: Synonym directory URL NOT set");
			ex.printStackTrace();
		}
	}

	public URL getParaDirURL() {
		return paraDirURL;
	}

	public void setParaFileURL(URL paraDirURL) {
		try {
			// This should not ever throw a malformed url exception
			this.paraDirURL = new URL(paraDirURL.toString());
		} catch (MalformedURLException ex) {
			System.err.println("Error: Synonym directory URL NOT set");
			ex.printStackTrace();
		}
	}

	public int getBeamSize() {
		return beamSize;
	}

	public void setBeamSize(int beamSize) {
		this.beamSize = beamSize;
	}

	public boolean getCharBased() {
		return charBased;
	}

	public void setCharBased(boolean charBased) {
		this.charBased = charBased;
	}

	public void newLang(String filesDir) {
		setLanguage("other");
		setTask("universal");
		try {
			setWordFileURL((new File(filesDir, "function.words")).toURI()
					.toURL());
			setParaFileURL((new File(filesDir, "paraphrase.gz")).toURI()
					.toURL());
		} catch (MalformedURLException ex) {
			System.err.println("Error: files NOT set");
			ex.printStackTrace();
		}
		setNormalization(Constants.NORMALIZE_LC_ONLY);
	}

	// Integration with Java properties
	public MeteorConfiguration(Properties props) {

		setDefaults();

		// Language
		String language = props.getProperty("language");
		if (language != null)
			setLanguage(language);

		// Task
		String task = props.getProperty("task");
		if (task != null)
			setTask(task);

		// Parameters
		String parameters = props.getProperty("parameters");
		if (parameters != null) {
			ArrayList<Double> params = new ArrayList<Double>();
			StringTokenizer p = new StringTokenizer(parameters);
			while (p.hasMoreTokens())
				params.add(Double.parseDouble(p.nextToken()));
			setParameters(params);
		}

		// Weights
		String weights = props.getProperty("moduleWeights");
		if (weights != null) {
			ArrayList<Double> weightList = new ArrayList<Double>();
			StringTokenizer wtok = new StringTokenizer(weights);
			while (wtok.hasMoreTokens())
				weightList.add(Double.parseDouble(wtok.nextToken()));
			setModuleWeights(weightList);
		}

		// Modules
		String modules = props.getProperty("modules");
		if (modules != null) {
			ArrayList<String> modList = new ArrayList<String>();
			StringTokenizer mods = new StringTokenizer(modules);
			while (mods.hasMoreTokens())
				modList.add(mods.nextToken());
			setModulesByName(modList);
			// Update weights to match number of modules
			ArrayList<Double> weightList = getModuleWeights();
			ArrayList<Double> sizedWeightList = new ArrayList<Double>();
			for (int i = 0; i < modList.size(); i++) {
				if (i < weightList.size())
					sizedWeightList.add(weightList.get(i));
				else
					sizedWeightList.add(0.0);
			}
			setModuleWeights(sizedWeightList);
		}

		// Beam size
		String beamSize = props.getProperty("beamSize");
		if (beamSize != null)
			setBeamSize(Integer.parseInt(beamSize));

		// Word list file
		String wordFile = (props.getProperty("wordFile"));
		if (wordFile != null)
			try {
				// This should not ever throw a malformed url exception
				setWordFileURL((new File(wordFile)).toURI().toURL());
			} catch (MalformedURLException ex) {
				System.err.println("Error: Word list directory URL NOT set");
				ex.printStackTrace();
			}

		// Synonym dir
		String synDir = (props.getProperty("synDir"));
		if (synDir != null)
			try {
				// This should not ever throw a malformed url exception
				setSynDirURL((new File(synDir)).toURI().toURL());
			} catch (MalformedURLException ex) {
				System.err.println("Error: Synonym directory URL NOT set");
				ex.printStackTrace();
			}

		// Paraphrase file
		String paraFile = (props.getProperty("paraFile"));
		if (paraFile != null)
			try {
				// This should not ever throw a malformed url exception
				setParaFileURL((new File(paraFile)).toURI().toURL());
			} catch (MalformedURLException ex) {
				System.err.println("Error: Paraphrase directory URL NOT set");
				ex.printStackTrace();
			}

		// Normalization
		Boolean norm = Boolean.parseBoolean(props.getProperty("norm"));
		Boolean lower = Boolean.parseBoolean(props.getProperty("lower"));
		Boolean noPunct = Boolean.parseBoolean(props.getProperty("noPunct"));
		Boolean nBest = Boolean.parseBoolean(props.getProperty("nBest"));

		if (nBest) {
			// NBest scoring handles its own normalization
			setNormalization(Constants.NO_NORMALIZE);
		} else if (norm) {
			if (noPunct)
				setNormalization(Constants.NORMALIZE_NO_PUNCT);
			else
				setNormalization(Constants.NORMALIZE_KEEP_PUNCT);
		} else if (lower) {
			setNormalization(Constants.NORMALIZE_LC_ONLY);
		} else {
			setNormalization(Constants.NO_NORMALIZE);
		}

		Boolean charBased = Boolean
				.parseBoolean(props.getProperty("charBased"));
		setCharBased(charBased);

		// New language overrides other options
		Boolean newLang = Boolean.parseBoolean(props.getProperty("newLang"));
		if (newLang) {
			newLang(props.getProperty("filesDir"));
		}
	}
}