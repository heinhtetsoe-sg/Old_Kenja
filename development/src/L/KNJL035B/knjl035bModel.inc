<?php

require_once('for_php7.php');


class knjl035bModel extends Model {
    var $cmd;
    var $control;
    var $selectdata;
    var $year;          //入試年度
    var $applicantdiv;
    var $testdiv;
    var $desirediv;

    function knjl035bModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
       if (VARS::get("PROGRAMID")) {
           unset($this->year);
           unset($this->applicantdiv);
           unset($this->testdiv);
           unset($this->desirediv);
       }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $tmpa = VARS::get("APPLICANTDIV");
        if(isset($tmpa)) {
            $this->applicantdiv = $tmpa;
        }

        $tmpt = VARS::get("TESTDIV");
        if(isset($tmpt)) {
            $this->testdiv = $tmpt;
        }

        $tmpd = VARS::get("DESIREDIV");
        if(isset($tmpd)){
            $this->desirediv = $tmpd;
        }

        $this->field = array("ENTEXAMYEAR"  =>  $this->year,                    //入試年度
                             "APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "DESIREDIV"    =>  VARS::post("DESIREDIV")         //志望区分
                             );
        //選択コース
        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
    }

    //更新前チェック
    function validateInput()
    {
        //必須項目
        if($this->field["APPLICANTDIV"] == ""){
            $this->setWarning("MSG301","（ 入試制度 ）");
            return false;
        }
        if($this->field["TESTDIV"] == ""){
            $this->setWarning("MSG301","（ 入試区分 ）");
            return false;
        }
        if($this->field["DESIREDIV"] == ""){
            $this->setWarning("MSG301","（ 志望区分 ）");
            return false;
        }
        if($this->field["DESIREDIV"] == 0){
            $this->setWarning("数字(1～9)を入力してください。（ 志望区分 ）");
            return false;
        }

        $db = Query::dbCheckOut();

        //選択コースについてのチェック
        if($this->selectdata == ""){
            $cntdesirediv = $db->getOne(knjl035bQuery::getCntWishdiv($this));
            if($cntdesirediv > 0){
                $this->setWarning("MSG300","この志望区分は使用されています。");
                return false;
            }
        }
        $chcourse = $db->getOne(knjl035bQuery::getChCntCourse($this));
        if($chcourse > 0){
            $this->setWarning("MSG300","このコースは既に合格学科で使用されています。");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    //次年度のデータを作成
    function &getCopyModel()
    {
        if ($this->year == (CTRL_YEAR + 1)) {
            //今年度のデータが存在するかチェック
            $t_year = knjl035bQuery::CheckData($this->year);
            if(!isset($t_year)){
               $this->setWarning('MSG203', "今年度のデータが存在しません。");
               return;
            }
        }
        if(knjl035bQuery::getCopyQuery($this))
        {
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" .($this->year+1). "年度の課程、学科マスタが存在していません。");
        }

    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjl035bQuery::getUpdateQuery($this);
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
