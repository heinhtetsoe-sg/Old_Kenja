<?php

require_once('for_php7.php');

class knjf080Model extends Model
{
    public $field      = array();
    public $schregno;      //学籍番号
    public $cmd;
    public $name;          //生徒氏名
    public $grade;
    public $getSchKind;
    public $isKumamoto;

    //更新するフィールド
    public $item = array();


    public function knjf080Model()
    {
    }
    public function init()
    {
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade     = VARS::get("GRADE");
        }
        if (VARS::post("GrCl")) {
            $this->GradeClass = VARS::post("GrCl");
        }
        $this->cmd       = VARS::request("cmd");
        if ($this->cmd == 'init' || VARS::get("PROGRAMID")) {
            unset($this->name);
            unset($this->schregno);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
        //学校判定
        $z010 = $db->getRow(knjf080Query::getNameMstZ010(), DB_FETCHMODE_ASSOC);
        $this->isKumamoto = ($z010["NAME1"] == 'kumamoto') ? true : false;

        //校種取得
        $query = knjf080Query::getSchoolKind($this->grade);
        $this->getSchKind = $db->getOne($query);
        Query::dbCheckIn($db);

        //更新するフィールド
        $this->item = array(
            "NUTRITION_RESULT",
            "EYEDISEASE_RESULT",
            "SKINDISEASE_RESULT",
            "SPINERIB_RESULT",
            "NOSEDISEASE_RESULT",
            "HEARTDISEASE_RESULT",
            "OTHERDISEASE_RESULT"
        );

        if ($this->isKumamoto) {
            $cnt = get_count($this->item);
            $this->item[$cnt++] = "D021_REMARK1";
            $this->item[$cnt++] = "D021_REMARK2";
        }

        foreach ($this->item as $field) {
            $this->field[$field] = VARS::post($field);
        }
    }
    //入力項目チェック
    public function validateInput($kind)
    {
        if (strlen($this->field["NUTRITION_RESULT"]) > 60) {
            $this->setWarning("MSG915", "内科 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["EYEDISEASE_RESULT"]) > 60) {
            $this->setWarning("MSG915", "眼科 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SKINDISEASE_RESULT"]) > 60) {
            $this->setWarning("MSG915", "皮膚科 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SPINERIB_RESULT"]) > 60) {
            $this->setWarning("MSG915", "胸郭・脊柱 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NOSEDISEASE_RESULT"]) > 60) {
            $this->setWarning("MSG915", "耳鼻科 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["HEARTDISEASE_RESULT"]) > 60) {
            $this->setWarning("MSG915", "心臓検診 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["OTHERDISEASE_RESULT"]) > 60) {
            $this->setWarning("MSG915", "その他 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["D021_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "視力 値（診察後の結果）は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["D021_REMARK2"]) > 60) {
            $this->setWarning("MSG915", "歯科 値（診察後の結果）は60バイトまでです。");
            return false;
        }

        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        knjf080Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    public function &getInsertModel()
    {
    }
    /**
     * 削除用のモデルを返します
     */
    public function &getDeleteModel()
    {
    }
    public function &delete()
    {
    }
    public function &insert()
    {
    }
    /**
     * アップデート時の内部メソッド
     */
    public function &update()
    {
    }
}
