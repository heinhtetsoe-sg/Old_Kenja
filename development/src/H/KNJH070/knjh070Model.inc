<?php

require_once('for_php7.php');

class knjh070Model extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持

    function knjh070Model()
    {
    }

    function init()
    {
        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
        }
        $this->field = array("TRAINDATE"    =>  VARS::post("TRAINDATE"),             //指導日付
                             "PATIENTCD"    =>  VARS::post("PATIENTCD"),             //相談者
                             "STAFFCD"      =>  VARS::post("STAFFCD"),               //対応者
                             "HOWTOTRAINCD" =>  VARS::post("HOWTOTRAINCD"),          //指導方法
                             "CONTENT"      =>  VARS::post("CONTENT"),               //指導内容
                             );

        $this->traindate    = VARS::request("TRAINDATE");
        $this->cmd          = VARS::request("cmd");

        if (VARS::request("SCHREGNO")){
            $this->schregno   = VARS::request("SCHREGNO");
        }
        if(VARS::get("GRADE")){
            $this->grade = VARS::get("GRADE");
        }
        if(VARS::get("HR_CLASS")){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if(VARS::get("ATTENDNO")){
            $this->attendno = VARS::get("ATTENDNO");
        }
        if(VARS::get("init") == 1){
            unset($this->schregno);
        }

        $this->getFieldSize();
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->cmd == "add") {
            if ($this->schregno=="") {
                   $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
                   return false;
            }
            if($this->field["TRAINDATE"]=="" || $this->field["PATIENTCD"]=="" || $this->field["STAFFCD"]=="" || $this->field["HOWTOTRAINCD"]==""){
                $this->setWarning("MSG301");
                return false;
            }

            //１レコード取得
            $db = Query::dbCheckOut();
            $row = $db->getRow(knjh070Query::selectQuery($this));
            Query::dbCheckIn($db);
            if (is_array($row)){
                $this->setWarning("MSG918");
                return false;
            }
        }

        $next_year = CTRL_YEAR+1;
        if($this->cmd == "add" || $this->cmd == "update"){
            $sdate = CTRL_YEAR."0401";
            $edate = $next_year."0331";
            $traindate = str_replace("/","",$this->traindate);
            if($traindate < $sdate || $traindate > $edate){
                $this->setWarning("MSG915","(処理年度範囲外)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["CONTENT"], ($this->content_moji * 2)) > $this->content_gyou) {
                $this->setWarning("MSG914","(指導内容は".$this->content_gyou."行までです)");
                return false;
            }
        }
        if($this->cmd == "update" || $this->cmd == "delete") {
            if($this->traindate == ""){
                $this->setWarning("MSG308");
                return false;
            }
            if($this->traindate != $this->field["TRAINDATE"]){
                $this->setWarning("MSG308");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
                $this->update();
        }
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->org_regddate = $this->field["REGDDATE"];
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }

    //デリート時の内部メソッド
    function &delete()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        $result = knjh070Query::getDeleteQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        $this->field = array();
        return $result;
    }

    //インサート時の内部メソッド
    function &insert()
    {
        $result = knjh070Query::getInsertQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh070Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //フィールドサイズの設定
    function getFieldSize() {
        $this->content_moji = 30;
        $this->content_gyou = 5;
    }
}
?>
