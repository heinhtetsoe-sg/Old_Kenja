<?php

require_once('for_php7.php');

class knjg080Model extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $selectstaffdata;
    var $selectstaffdata2;
    var $selectguardiandata;
    var $selectguardiandata2;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjg080Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHKIND"  =>  VARS::post("SCHKIND"),           //校種
                             "SENTAKU"  =>  VARS::post("SENTAKU")            //ラジオボタン
                             );
    
        $this->hrClass = VARS::post("GRADE_HR_CLASS");
        $this->div = VARS::post("DIV");
        
        $this->year = VARS::post("STAFF_YEAR");
        
        $this->selectdata   = VARS::post("selectdata");     //対象者一覧
        $this->selectdata2  = VARS::post("selectdata2");    //生徒一覧
        
        $this->selectstaffdata   = VARS::post("selectstaffdata");     //職員対象者一覧
        $this->selectstaffdata2  = VARS::post("selectstaffdata2");    //職員一覧

        $this->selectguardiandata   = VARS::post("selectguardiandata");     //対象者一覧(保護者)
        $this->selectguardiandata2  = VARS::post("selectguardiandata2");    //生徒一覧（保護者）

        $this->checked      = VARS::request("CHECKED");     //通称名印字チェック

        $this->cmd          = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function validateInput() {
        return true;
    }

    function changeView() {
        $this->field["SENTAKU"] = '2';
        return true;
    }

    function changeView2() {
        $this->field["SENTAKU"] = '3';
        return true;
    }
    
/************************************
****生徒用************************
************************************/

    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        if(implode(',', $db->getcol(knjg080Query::getStudent_left($this))) != $this->selectdata){
            if($this->selectdata){
                knjg080Query::getDeleteQuery($db, $this, "1");
                $schregno = explode(',', $this->selectdata);
                for ($i = 0; $i < get_count($schregno); $i++){
                    $flg = $db->getOne(knjg080Query::getNameOutputFlg($this, $schregno[$i]));
                    knjg080Query::getUpdateQuery($db, $this, $schregno[$i], $flg);
                }
            } else {
                knjg080Query::getDeleteQuery($db, $this);
            }
        }
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return $result;
    }
    
    //詳細設定
    function &getUpdateModel2() {
        knjg080Query::getUpdateQuery2($this);
        $this->setMessage("MSG201");
        return $result;
    }

/************************************
****職員用************************
************************************/

    //職員用
    function &getUpdateModel_staff() {
        $db = Query::dbCheckOut();

        if(implode(',', $db->getcol(knjg080Query::getStaff_left($this))) != $this->selectstaffdata){
            if($this->selectstaffdata){
                knjg080Query::getDeleteQuery_staff($db, $this, "1");
                $staffcd = explode(',', $this->selectstaffdata);
                for ($i = 0; $i < get_count($staffcd); $i++){
                    $flg = $db->getOne(knjg080Query::getNameOutputFlg_staff($this, $staffcd[$i]));
                    knjg080Query::getUpdateQuery_staff($db, $this, $staffcd[$i], $flg);
                }
            } else {
                knjg080Query::getDeleteQuery_staff($db, $this);
            }
        }
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return $result;
    }

    //詳細設定
    function &getUpdateModel2_staff() {
        knjg080Query::getUpdateQuery2_staff($this);
        $this->setMessage("MSG201");
        return $result;
    }

/************************************
****保護者用************************
************************************/

    function &getUpdateModel_guardian() {
        $db = Query::dbCheckOut();

        if(implode(',', $db->getcol(knjg080Query::getGuardian_left($this))) != $this->selectguardiandata){
            if($this->selectguardiandata){
                knjg080Query::getDeleteQuery_guardian($db, $this, "1");
                $schregno = explode(',', $this->selectguardiandata);
                for ($i = 0; $i < get_count($schregno); $i++){
                    $flg = $db->getOne(knjg080Query::getGuardNameOutputFlg($this, $schregno[$i]));
                    knjg080Query::getUpdateQuery_guardian($db, $this, $schregno[$i], $flg);
                }
            } else {
                knjg080Query::getDeleteQuery_guardian($db, $this);
            }
        }
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return $result;
    }
    
    //詳細設定
    function &getUpdateModel2_guardian() {
        knjg080Query::getUpdateQuery2_guardian($this);
        $this->setMessage("MSG201");
        return $result;
    }
    
}
?>
