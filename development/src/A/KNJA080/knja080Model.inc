<?php

require_once('for_php7.php');

class knja080Model extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $control;
    var $min_semester;  //最小学期
    var $schregno;      //学籍番号（左リスト）
    var $schoolName;
    var $schoolDiv;
    
    function knja080Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {           
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::request("selectdata");
        if (VARS::get("PROGRAMID")){
            unset($this->newclass);
            unset($this->oldclass);
        }
        if (VARS::get("NEWCLASS")){
            $this->newclass     = VARS::get("NEWCLASS");
        }
        if (VARS::get("OLDCLASS")){
            $this->oldclass     = VARS::get("OLDCLASS");
        }
        $rtnRow = knja080Query::getNameMst();
        $this->schoolName           = $rtnRow["NAME1"];     // 学校区分
        $this->schoolDiv            = $rtnRow["NAMESPARE2"];// 1:併設校（法政・智辯） 2:中等教育学校（九段・自修館）
        $this->isIkkanUseSchKind    = ($rtnRow["NAMESPARE2"] == "2" && $this->Properties["use_prg_schoolkind"] != "1" && $this->Properties["useSchool_KindField"] == "1") ? "1" : "";
    }

    function validateInput()
    {
        if (!isset($this->newclass)){
            $this->setWarning('MSG301');
            return false;
        }
        return true;
    }

    //新･旧クラス選択表示
    function &selectclass()
    {
        $db = Query::dbCheckOut();
        //新クラス読込み
        $this->getNewReadModel($db);
        //旧クラス読込み
        $this->getOldReadModel($db);

        Query::dbCheckIn($db);
    }
    //新クラス生徒読込
    function &getNewReadModel(&$db)
    {
        //新クラス一覧
        $result = $db->query(knja080Query::GetNewStudent($this));
        $i = 0;
        $this->schregno = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row = str_replace("'","\'",$row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAME"] = ($row["NAME_SHOW"]) ? $row["NAME_SHOW"] : $row["NAME"];
            $remaingrade  = ($row["REMAINGRADE_FLG"] == "1") ? " [留] " : "　　　";
            $arg["NEW_OPTION"][$i] = array("TEXT" => $row["HR_NAME"].$row["OLD_ATTENDNO"] ."番"."　".$remaingrade."　".$row["SCHREGNO"]."　".$row["NAME"],
                                           "VALUE" => $row["SCHREGNO"]
                                           );

            $i++;
            $this->schregno[] = $row["SCHREGNO"];
        }

        //インラインフレームに表示
        View::t_include("knja080Form2.html", $arg);
    }
    //旧クラス生徒読込
    function &getOldReadModel(&$db)
    {
        //旧クラス一覧
        $result = $db->query(knja080Query::GetOldStudent($this));
        $i = 0;

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace("'","\'",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $remaingrade  = ($row["REMAINGRADE_FLG"] == "1") ? " [留] " : "　　　";
            $arg["OLD_OPTION"][$i] = array("TEXT" => $row["HR_NAME"].$row["ATTENDNO"] ."番"."　".$remaingrade."　".$row["SCHREGNO"]."　". $row["NAME"],
                                           "VALUE" => $row["SCHREGNO"]
                                           );
            $i++;
        }
        //インラインフレームに表示
        View::t_include("knja080Form2.html", $arg);
    }
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            knja080Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
