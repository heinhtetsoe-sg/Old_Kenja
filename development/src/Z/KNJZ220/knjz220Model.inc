<?php

require_once('for_php7.php');

class knjz220Model extends Model {

    var $field1      = array();
    var $field2      = array();
    var $cmd;
    var $grade;
    var $classcd;
    var $copy;
    var $copy_flg;
    var $copy_subclasscd;
    var $copy_grade;
    var $Clean;
    var $sec_competence;            //権限保持
    var $subclassname;
    var $TBL_COUNT;                 //テーブルの最大レコード数
    var $default_val = array();     //デフォルト値

    //Constructor
    function knjz220Model() {}

    function init()
    {   
        if (VARS::get("PROGRAMID") || VARS::get("ini")) {
            //unset($this->classcd);
            //unset($this->subclassname);
            $this->grade = "";
            $this->classcd = "";
            $this->subclassname = "";

        }
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //セキュリティーチェック値
        $this->sec_competence = AUTHORITY;

        //学年
        $db = Query::dbCheckOut();
        $result = $db->query(knjz220Query::combo_grdQuery($this));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        if (!$this->grade) $this->grade = $row["GRADE"];
        Query::dbCheckIn($db);

        if (VARS::request("GRADE"))     $this->grade        = VARS::request("GRADE");
        if (VARS::post("CLASSCD"))      $this->classcd      = VARS::post("CLASSCD");
        if (VARS::get("SUBCLASSNAME"))  $this->subclassname = VARS::get("SUBCLASSNAME");

        //デフォルト値取得
        $this->default_val = array( "LOW"  => VARS::post("default_val_low"),
                                    "HIGH" => VARS::post("default_val_high") );
        //フィールドのレコード数取得
        $this->TBL_COUNT = VARS::post("TBL_COUNT");

        //最大値、最小値を取得
        if($this->default_val["LOW"] != "" && $this->default_val["HIGH"] != ""){
            $min = explode(",",$this->default_val["LOW"]);
            $max = explode(",",$this->default_val["HIGH"]);
            $this->default_val["LOW"]  = $min[0];
            $this->default_val["HIGH"] = $max[((int)$this->TBL_COUNT-1)];
        }

        $this->field1 = array( "SUBCLASSCD"     =>  VARS::request("SUBCLASSCD"),   //科目コード
                               "UPDATED"        =>  VARS::post("UPDATED"));        //更新日付

        //テーブルのレコードの数だけ取得
        for($i=1; $i<=$this->TBL_COUNT; $i++){
            $this->field2["ASSESSLOW".($i)]  = VARS::post("ASSESSLOW".($i));
            $this->field2["ASSESSMARK".($i)] = VARS::post("ASSESSMARK".($i));
        }
        //$this->field2["UPDATED"] = "";

        $this->cmd          = VARS::request("cmd");
        $this->Clean        = VARS::post("Cleaning");
        $this->copy         = VARS::request("copy");
        $this->copy_flg     = false;
    }

    function validateInput()
    {
        $chk[] = array();

        //field値チェック
        if ($this->field1["SUBCLASSCD"] == ""){
            $this->setWarning("MSG303");
            return false;
        }

        if ($this->grade == ""){
            $this->setWarning("MSG303");
            return false;
        }

        //UPDATEチェック
        if(!is_null($this->field1["UPDATED"]) && $this->field1["UPDATED"] != ""){

            //DBオープン
            $db     = Query::dbCheckOut();
            $result = $db->query(knjz220Query::selectQuery($this));
            $up     = "";

            while($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($up == ""){
                    $up  =     $row["UPDATED"];
                }else{
                    $up .= ",".$row["UPDATED"];
                }
            }
            //DBクローズ
            Query::dbCheckIn($db);

            if($this->field1["UPDATED"] != $up){
                $this->setWarning("MSG307");
                return false;
            }
        }

        //最大値チェック
        if (!isset($this->TBL_COUNT)){
            $this->setWarning("MSG917");
            return false;
        }

        //テキストエリア入力チェック
        for($i=2; $i<=$this->TBL_COUNT; $i++)
        {

            $ASSESSLOW  = "ASSESSLOW".($i);

            if(!isset($this->field2[$ASSESSLOW])){
                $this->setWarning("MSG301");
                return false;
            }

            //空かどうか？
            if($this->field2[$ASSESSLOW] == ""){
                $this->setWarning("MSG301");
                return false;
            }

            //数字かどうか？
            if(!is_numeric($this->field2[$ASSESSLOW])){
                $this->setWarning("MSG301");
                return false;
            }

            // 最大値 or 最小値 かどうか？
            if($this->field2[$ASSESSLOW] <= $this->default_val["LOW"]){
                $this->setWarning("MSG901");
                return false;
            } else if($this->field2[$ASSESSLOW] >= $this->default_val["HIGH"]){
                $this->setWarning("MSG901");
                return false;
            }
            $chk[] = $this->field2[$ASSESSLOW];
        }

        //値大小チェック
        for($i=1; $i<($this->TBL_COUNT); $i++)
        {
            #echo $i;
            if($i == 1){
                #echo $i."min ".$chk[$i]." >= ".$this->default_val["LOW"]." <BR>";
                if($chk[$i] <= $this->default_val["LOW"]){
                    $this->setWarning("MSG901");
                    return false;
                }
            }else if($i == ((int)$this->TBL_COUNT - 1)){
                #echo $i."max ".$chk[$i]." >= ".$this->default_val["HIGH"]." <BR>";
                if($chk[$i] >= $this->default_val["HIGH"]){
                    $this->setWarning("MSG901");
                    return false;
                }
            }else{
                #echo $i."else ".$chk[$i]." >= ".$chk[($i + 1)]." <BR>";
                if($chk[$i] >= $chk[($i + 1)]){
                    $this->setWarning("MSG901");
                    return false;
                }
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                     $this->setMessage("MSG201");
            }
        }
    }

    function &update()
    {
        //評定マスタデータ削除
        $result = knjz220Query::getDeleteQuery($this);
        //評定マスタデータ追加
        $result = knjz220Query::getInsertQuery($this);

        return $result;
    }

    function copy_data()
    {
        $ar[] = array();
        $ar = explode(",",$this->copy);

        $this->copy_grade       = $ar[0];
        $this->copy_subclasscd  = $ar[1];
        $this->copy_flg         = true ;

        return true;
    }
}
?>
