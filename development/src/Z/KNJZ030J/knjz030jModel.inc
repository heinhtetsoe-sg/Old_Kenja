<?php

require_once('for_php7.php');


class knjz030jModel extends Model {
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $year;                  //入試年度
    var $testdiv;
    var $desirediv;
    var $selectdata;

    function knjz030jModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "knjz030j");

       if (VARS::get("PROGRAMID")) {
           $this->desirediv = "";
           $this->testdiv = "";
       }

        $this->cmd  = VARS::request("cmd");
        if (strlen(VARS::request("year"))) $this->examyear = VARS::request("year");

        $this->field = array("TESTDIV"          =>  VARS::post("TESTDIV"),	        //入試区分
							 "DESIREDIV"        =>  VARS::post("DESIREDIV"),	    //志望区分
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $tmpd = VARS::get("DESIREDIV");
        if(isset($tmpd)){
            $this->desirediv = $tmpd;
        }

        $tmpd = VARS::get("TESTDIV");
        if(isset($tmpd)){
            $this->testdiv = $tmpd;
        }

        $temp = VARS::post("TESTDIV");
        if (isset($temp)) {
            $this->testdiv          = VARS::post("TESTDIV");
            unset($this->desirediv);
        }

        //選択コース
        $this->selectdata = VARS::post("selectdata");

    }

    //更新前チェック
    function validateInput()
    {
        //必須項目
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
        $selectarr = explode(",",$this->selectdata);
        if(get_count($selectarr) > 1){
            $this->setWarning("MSG300","選択できるのは１つまでです。");
            return false;
        }
        if($this->selectdata == ""){
            $chcourse = $db->getOne(knjz030jQuery::getChCntCourse($this));
            $cntdesirediv = $db->getOne(knjz030jQuery::getCntDesirediv($this));
            if($chcourse > 0){
                $this->setWarning("MSG300","このコースは合格学科で使用されています。");
                return false;
            }
            elseif($cntdesirediv > 0){
                $this->setWarning("MSG300","この志望区分は使用されています。");
                return false;
            }
        }
        $chcourse = $db->getOne(knjz030jQuery::getChCntCourse($this));
        if($chcourse > 0){
            $this->setWarning("MSG300","このコースは合格学科で使用されています。");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel()
    {
        if(knjz030jQuery::getCopyQuery($this))
        {
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" .($this->examyear+1). "年度の課程、学科マスタが存在していません。");
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
        knjz030jQuery::getUpdateQuery($this);
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
