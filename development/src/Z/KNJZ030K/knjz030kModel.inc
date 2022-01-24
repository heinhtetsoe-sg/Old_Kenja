<?php

require_once('for_php7.php');


class knjz030kModel extends Model {
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $selectdata;

    function knjz030kModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
#        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "knjz030k");

       if (VARS::get("PROGRAMID")) {
           unset($this->testdiv);
           unset($this->desirediv);
           unset($this->examyear);
       }

        $this->cmd  = VARS::request("cmd");
        if (strlen(VARS::request("year"))) $this->examyear = VARS::request("year");

        $tmpt = VARS::get("TESTDIV");
        if(isset($tmpt)) {
            $this->testdiv = $tmpt;
        }
        $tmpd = VARS::get("DESIREDIV");
        if(isset($tmpd)){
            $this->desirediv = $tmpd;
        }

        $this->field = array("TESTDIV"   =>  VARS::post("TESTDIV"),     //試験区分
                             "DESIREDIV" =>  VARS::post("DESIREDIV")    //志望区分
                             );
        //選択コース
        $this->selectdata = VARS::post("selectdata");
    }

    //更新前チェック
    function validateInput()
    {
        //必須項目
        if($this->field["TESTDIV"] == ""){
            $this->setWarning("MSG301","（ 試験区分 ）");
            return false;
        }
        if($this->field["DESIREDIV"] == ""){
            $this->setWarning("MSG301","（ 志望区分 ）");
            return false;
        }
//2006.01.25 alp m-yama
/*
        if($this->field["DESIREDIV"] == 0){
            $this->setWarning("数字(1～9)を入力してください。（ 志望区分 ）");
            return false;
        }
*/
        $db = Query::dbCheckOut();

        //選択コースについてのチェック
        if($this->selectdata == ""){
            $cntdesirediv = $db->getOne(knjz030kQuery::getCntWishdiv($this));
            if($cntdesirediv > 0){
                $this->setWarning("MSG300","この志望区分は使用されています。");
                return false;
            }
        }
        $chcourse = $db->getOne(knjz030kQuery::getChCntCourse($this));
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
#        $cntdiv = knjz030kQuery::cntWishdiv($this->examyear);
#        if($cntdiv > 0){
#            $this->setWarning('MSG203', $this->examyear."年度のデータは存在します。");
#            return false;
#        }else{
#            knjz030kQuery::getCopyQuery($this->examyear);
#        }
        if ($this->examyear == (CTRL_YEAR + 1)) {
            //今年度のデータが存在するかチェック
            $t_year = knjz030kQuery::CheckData($this->examyear);
            if(!isset($t_year)){
               $this->setWarning('MSG203', "今年度のデータが存在しません。");
               return;
            }
        }
        if(knjz030kQuery::getCopyQuery($this))
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
            $this->field["DESIREDIV"] = sprintf('%02d', $this->field["DESIREDIV"]);
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz030kQuery::getUpdateQuery($this);
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
