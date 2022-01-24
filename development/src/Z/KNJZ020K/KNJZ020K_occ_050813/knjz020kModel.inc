<?php

require_once('for_php7.php');

//定数定義
class knjz020kModel extends Model {
    var $field      = array();
    var $cmd;
    var $examcoursecd;        //primary
    var $NextYear;
    var $course;            //コース

    //メンバ変数の初期化
    function knjz020kModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->course);
            unset($this->testdiv);
            unset($this->testsubclasscd);
            unset($this->adotestdiv);
        }

        $this->cmd = VARS::request("cmd");

        $temp = VARS::request("COURSE");
        if (isset($temp)) {
            $this->course = VARS::request("COURSE");
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("YEAR"              => VARS::post("YEAR"),
                             "TESTDIV"           => VARS::post("TESTDIV"),
                             "TESTSUBCLASSCD"    => VARS::post("TESTSUBCLASSCD"),
                             "COURSE"            => $this->course,
                             "A_PERFECT"         => VARS::post("A_PERFECT"),            //満点
                             "AUTOCALC"          => VARS::post("AUTOCALC"),             //自動計算
                             "INC_MAGNIFICATION" => VARS::post("INC_MAGNIFICATION")     //傾斜倍率
                             );

        if (VARS::get("TESTDIV")) {
            $this->testdiv = VARS::get("TESTDIV");
        }
        if (VARS::get("TESTSUBCLASSCD")) {
            $this->testsubclasscd = VARS::get("TESTSUBCLASSCD");
        }

        if($this->cmd == "change_testdiv"){
            $this->adotestdiv = VARS::post("ADOTESTDIV");
        }

        //選択科目
        $this->selectdata = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["TESTDIV"]) == 0){
            $this->setWarning("MSG301","(試験区分)");
            return false;
        }
        if(strlen($this->field["TESTSUBCLASSCD"]) == 0){
            $this->setWarning("MSG301","(試験科目)");
            return false;
        }
        if(strlen($this->field["COURSE"]) == 0){
            $this->setWarning("MSG301","(コース)");
            return false;
        }
        if($kind=="insert" || $kind=="update"){
            if(strlen($this->field["A_PERFECT"]) == 0){
                $this->setWarning("MSG301","(Ａ配点の満点)");
                return false;
            }
            //満点に０点は設定できない
            if($this->field["A_PERFECT"] == 0){
                $this->setWarning("MSG901","Ａ配点の満点で\"0\"は設定できません。");
                return false;
            }
            //傾斜倍率の範囲は0～9.9
            $inc_magnification = explode(".",$this->field["INC_MAGNIFICATION"]);
            if(strlen($inc_magnification[0]) > 1){
                $this->setWarning("MSG915","傾斜倍率は0～9.9です。");
                return false;
            }
        }
        //キー値が変更されていますかをチェック
        if(($kind=="update" || $kind=="delete") && $this->testdiv != $this->field["TESTDIV"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if(($kind=="update" || $kind=="delete") && $this->testsubclasscd != $this->field["TESTSUBCLASSCD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //ENTEXAM_SCORE_DATのMAX(score)と更新する満点との比較
        if($kind=="update"){
            $maxscore = knjz020kQuery::ChScoreDat($this);
            if($maxscore > $this->field["A_PERFECT"]){
                $this->setWarning("MSG300","入力されている".$maxscore."点より低い満点は設定出来ません。");
                return false;
            }
        }
        //ENTEXAM_SCORE_DATで使用されてないかチェック
        if($kind=="delete"){
            $scoredat = knjz020kQuery::ChScoreDat($this);
            if($scoredat > 0){
                $this->setWarning("MSG300","得点が設定されています。");
                return false;
            }
        }

        //入力項目の１レコード取得
        $row = knjz020kQuery::getRow($this,2);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
#        //更新日付チェック1
#        if ($this->cmd == "update" && $row["UPDATED"] == ""){
#            $this->setWarning("MSG303");
#            return false;
#        }
#        //更新日付チェック2
#        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
#            $this->setWarning("MSG307");
#            return false;
#        }
        //文字数チェック
        if (strlen($this->field["A_PERFECT"]) > 3){
            $this->setWarning("MSG915","(Ａ配点の満点は3バイトまでです。)");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjz020kQuery::getInsertQuery($this);
        }
        $this->testdiv        = $this->field["TESTDIV"];
        $this->testsubclasscd = $this->field["TESTSUBCLASSCD"];
        $this->course         = $this->field["COURSE"];
        $this->setMessage("MSG201");
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjz020kQuery::getUpdateQuery($this);
        }
        $this->testdiv        = $this->field["TESTDIV"];
        $this->testsubclasscd = $this->field["TESTSUBCLASSCD"];
        $this->course         = $this->field["COURSE"];
        $this->setMessage("MSG201");
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjz020kQuery::getDeleteQuery($this);
            $this->testdiv        = "";
            $this->testsubclasscd = "";
        }
        $this->course         = $this->field["COURSE"];
        $this->setMessage("MSG201");
        return;
    }

    //次年度のデータを作成
    function &getCopyModel()
    {
#        if($cntdiv > 0){
#            $this->setWarning('MSG203', $this->examyear."年度のデータは存在します。");
#            return false;
#        }else{
#            knjz020kQuery::getCopyQuery($this);
#        }
        if ($this->year == (CTRL_YEAR + 1)) {
            //今年度のデータが存在するかチェック
            $t_year = knjz020kQuery::CheckData($this->year);
            if(!isset($t_year)){
               $this->setWarning('MSG203', "今年度のデータが存在しません。");
               return;
            }
        }
        $this->NextYear = $this->year + 1;
        if(knjz020kQuery::getCopyYearQuery($this))
        {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" . "$this->NextYear" . "年度の受験コースマスタか\\n名称マスタが存在していません。");
        }
    }

    //アラカルト科目指定：更新処理
    function &getAdoUpdateModel()
    {
        knjz020kQuery::getAdoUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
