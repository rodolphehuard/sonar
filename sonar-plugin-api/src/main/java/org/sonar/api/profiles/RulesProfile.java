/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.api.profiles;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sonar.api.database.model.ResourceModel;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is badly named. It should be "QualityProfile". Indeed it does not relate only to rules but to metric thresholds too.
 */
@Entity
@Table(name = "rules_profiles")
public class RulesProfile implements Cloneable {

  /**
   * Name of the default profile "Sonar Way"
   */
  public static final String SONAR_WAY_NAME = "Sonar way";

  /**
   * Name of the default java profile "Sonar way with Findbugs"
   */
  public static final String SONAR_WAY_FINDBUGS_NAME = "Sonar way with Findbugs";

  /**
   * Name of the default java profile "Sun checks"
   */
  public static final String SUN_CONVENTIONS_NAME = "Sun checks";

  @Id
  @Column(name = "id")
  @GeneratedValue
  private Integer id;

  @Column(name = "name", updatable = true, nullable = false)
  private String name;

  @Column(name = "default_profile", updatable = true, nullable = false)
  private Boolean defaultProfile = Boolean.FALSE;

  @Column(name = "provided", updatable = true, nullable = false)
  private Boolean provided = Boolean.FALSE;

  @Column(name = "enabled", updatable = true, nullable = false)
  private Boolean enabled = Boolean.TRUE;

  @Column(name = "language", updatable = true, nullable = false)
  private String language;

  @Column(name = "parent_name", updatable = true, nullable = true)
  private String parentName;

  @OneToMany(mappedBy = "rulesProfile", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
  private List<ActiveRule> activeRules = Lists.newArrayList();

  @OneToMany(mappedBy = "rulesProfile", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
  private List<Alert> alerts = Lists.newArrayList();

  @OneToMany(mappedBy = "rulesProfile", fetch = FetchType.LAZY)
  private List<ResourceModel> projects = Lists.newArrayList();

  /**
   * @deprecated use the factory method create()
   */
  @Deprecated
  public RulesProfile() {
  }

  /**
   * @deprecated since 2.3. Use the factory method create()
   */
  @Deprecated
  public RulesProfile(String name, String language) {
    this.name = name;
    this.language = language;
    this.activeRules = Lists.newArrayList();
    this.alerts = Lists.newArrayList();
    this.projects = Lists.newArrayList();
  }

  /**
   * @deprecated since 2.3. Use the factory method create()
   */
  @Deprecated
  public RulesProfile(String name, String language, boolean defaultProfile, boolean provided) {
    this(name, language);
    this.defaultProfile = defaultProfile;
    this.provided = provided;
  }

  public Integer getId() {
    return id;
  }

  /**
   * @return the profile name, unique by language.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the profile name.
   */
  public RulesProfile setName(String s) {
    this.name = s;
    return this;
  }

  /**
   * @return the list of active rules
   */
  public List<ActiveRule> getActiveRules() {
    return getActiveRules(false);
  }

  /**
   * @return the list of active rules
   */
  public List<ActiveRule> getActiveRules(boolean acceptDisabledRules) {
    if (acceptDisabledRules) {
      return activeRules;
    }
    List<ActiveRule> result = Lists.newArrayList();
    for (ActiveRule activeRule : activeRules) {
      if (activeRule.isEnabled()) {
        result.add(activeRule);
      }
    }
    return result;
  }

  public RulesProfile removeActiveRule(ActiveRule activeRule) {
    activeRules.remove(activeRule);
    return this;
  }

  public RulesProfile addActiveRule(ActiveRule activeRule) {
    activeRules.add(activeRule);
    return this;
  }

  /**
   * Set the list of active rules
   */
  public void setActiveRules(List<ActiveRule> activeRules) {
    this.activeRules = activeRules;
  }

  /**
   * @return whether this is the default profile for the language
   */
  public Boolean getDefaultProfile() {
    return defaultProfile;
  }

  /**
   * Set whether this is the default profile for the language. The default profile is used when none is explicitly defined when auditing a
   * project.
   */
  public void setDefaultProfile(Boolean b) {
    this.defaultProfile = b;
  }

  /**
   * @return whether the profile is defined in a plugin. Provided profiles are automatically restored during server startup and can not be
   *         updated by end users.
   */
  public Boolean getProvided() {
    return provided;
  }

  /**
   * Set whether the profile is provided by a plugin
   */
  public void setProvided(Boolean b) {
    this.provided = b;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public boolean isEnabled() {
    return enabled == Boolean.TRUE;
  }

  public RulesProfile setEnabled(Boolean b) {
    this.enabled = b;
    return this;
  }

  /**
   * @return the profile language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set the profile language
   */
  public RulesProfile setLanguage(String s) {
    this.language = s;
    return this;
  }

  /**
   * For internal use only.
   *
   * @since 2.5
   */
  public String getParentName() {
    return parentName;
  }

  /**
   * For internal use only.
   *
   * @since 2.5
   */
  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  /**
   * @return the list of alerts defined in the profile
   */
  public List<Alert> getAlerts() {
    return alerts;
  }

  /**
   * Sets the list of alerts for the profile
   */
  public void setAlerts(List<Alert> alerts) {
    this.alerts = alerts;
  }

  /**
   * @return the list of projects attached to the profile
   */
  public List<ResourceModel> getProjects() {
    return projects;
  }

  /**
   * Sets the list of projects attached to the profile
   */
  public void setProjects(List<ResourceModel> projects) {
    this.projects = projects;
  }

  /**
   * Note: disabled rules are excluded.
   * @return the list of active rules for a given severity
   */
  public List<ActiveRule> getActiveRules(RulePriority severity) {
    List<ActiveRule> result = Lists.newArrayList();
    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getSeverity().equals(severity) && activeRule.isEnabled()) {
        result.add(activeRule);
      }
    }
    return result;
  }

  /**
   * @deprecated since 2.3 use {@link #getActiveRulesByRepository(String)} instead.
   */
  @Deprecated
  public List<ActiveRule> getActiveRulesByPlugin(String repositoryKey) {
    return getActiveRulesByRepository(repositoryKey);
  }

  /**
   * Get the active rules of a specific repository.
   * Only enabled rules are selected. Disabled rules are excluded.
   */
  public List<ActiveRule> getActiveRulesByRepository(String repositoryKey) {
    List<ActiveRule> result = Lists.newArrayList();
    for (ActiveRule activeRule : activeRules) {
      if (repositoryKey.equals(activeRule.getRepositoryKey()) && activeRule.isEnabled()) {
        result.add(activeRule);
      }
    }
    return result;
  }

  /**
   * Note: disabled rules are excluded.
   * @return an active rule from a plugin key and a rule key if the rule is activated, null otherwise
   */
  public ActiveRule getActiveRule(String repositoryKey, String ruleKey) {
    for (ActiveRule activeRule : activeRules) {
      if (StringUtils.equals(activeRule.getRepositoryKey(), repositoryKey) && StringUtils.equals(activeRule.getRuleKey(), ruleKey) && activeRule.isEnabled()) {
        return activeRule;
      }
    }
    return null;
  }

  /**
   * Note: disabled rules are excluded.
   */
  public ActiveRule getActiveRuleByConfigKey(String repositoryKey, String configKey) {
    for (ActiveRule activeRule : activeRules) {
      if (StringUtils.equals(activeRule.getRepositoryKey(), repositoryKey) && StringUtils.equals(activeRule.getConfigKey(), configKey) && activeRule.isEnabled()) {
        return activeRule;
      }
    }
    return null;
  }

  /**
   * Note: disabled rules are excluded.
   */

  public ActiveRule getActiveRule(Rule rule) {
    return getActiveRule(rule.getRepositoryKey(), rule.getKey());
  }

  /**
   * @param optionalSeverity if null, then the default rule severity is used
   */
  public ActiveRule activateRule(Rule rule, RulePriority optionalSeverity) {
    ActiveRule activeRule = new ActiveRule();
    activeRule.setRule(rule);
    activeRule.setRulesProfile(this);
    activeRule.setSeverity(optionalSeverity == null ? rule.getSeverity() : optionalSeverity);
    activeRules.add(activeRule);
    return activeRule;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RulesProfile)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    RulesProfile other = (RulesProfile) obj;
    return new EqualsBuilder().append(language, other.getLanguage()).append(name, other.getName()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(language).append(name).toHashCode();
  }

  @Override
  public Object clone() {
    RulesProfile clone = RulesProfile.create(getName(), getLanguage());
    clone.setDefaultProfile(getDefaultProfile());
    clone.setProvided(getProvided());
    clone.setParentName(getParentName());
    if (CollectionUtils.isNotEmpty(activeRules)) {
      clone.setActiveRules(new ArrayList<ActiveRule>(CollectionUtils.collect(activeRules, new Transformer() {
        public Object transform(Object input) {
          return ((ActiveRule) input).clone();
        }
      })));
    }
    if (CollectionUtils.isNotEmpty(getAlerts())) {
      clone.setAlerts(new ArrayList<Alert>(CollectionUtils.collect(getAlerts(), new Transformer() {
        public Object transform(Object input) {
          return ((Alert) input).clone();
        }
      })));
    }
    if (CollectionUtils.isNotEmpty(getProjects())) {
      clone.setProjects(new ArrayList<ResourceModel>(CollectionUtils.collect(getProjects(), new Transformer() {
        public Object transform(Object input) {
          return ((ResourceModel) input).clone();
        }
      })));
    }
    return clone;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("[name=").append(name).append(",language=").append(language).append("]").toString();
  }

  public static RulesProfile create(String name, String language) {
    return new RulesProfile().setName(name).setLanguage(language);
  }

  public static RulesProfile create() {
    return new RulesProfile();
  }
}
