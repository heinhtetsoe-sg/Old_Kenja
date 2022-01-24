<?php

require_once('for_php7.php');


class knjz070_3Model extends Model {
    var $field      = array();
    var $cmd;
    var $year;
    var $leftyear = "";
    var $classcd = "";
    var $school_kind = "";      
    var $curriculum_cd = "";
    var $subclasscd = "";
    
    function knjz070_3Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "CLASSCD_SET"  =>  VARS::post("CLASSCD_SET"),//教科コンボ
                             "SUBCLASSCD_SET"  =>  VARS::request("SUBCLASSCD_SET"),
                             "SUBCLASSNAME"    =>  VARS::request("SUBCLASSNAME"),
                             
                             "SEQ_001"      =>  VARS::post("SEQ_001"),
                             /*"SEQ_002"      =>  VARS::post("SEQ_002"),
                             "REMARK1_002"  =>  VARS::post("REMARK1_002"),
                             "REMARK2_002"  =>  VARS::post("REMARK2_002"),
                             "SEQ_003"      =>  VARS::post("SEQ_003"),
                             "REMARK1_003"  =>  VARS::post("REMARK1_003"),*/
                             "SEQ_004"      =>  VARS::post("SEQ_004"),
                             "SEQ_005"      =>  VARS::post("SEQ_005"),
                             "SEQ_006"      =>  VARS::post("SEQ_006"),
                             "SEQ_007"      =>  "1",
                             "REMARK1_007"  =>  VARS::post("REMARK1_007"),
                             "REMARK2_007"  =>  VARS::post("REMARK2_007"),
                             "REMARK3_007"  =>  VARS::post("REMARK3_007"),
                             "REMARK4_007"  =>  VARS::post("REMARK4_007"),
                             "SEQ_008"      =>  "1",
                             "REMARK1_008"  =>  VARS::post("REMARK1_008"),
                             "REMARK2_008"  =>  VARS::post("REMARK2_008"),
                             "REMARK3_008"  =>  VARS::post("REMARK3_008"),
                             "REMARK4_008"  =>  VARS::post("REMARK4_008"),
                             "SEQ_009"      =>  VARS::post("SEQ_009"),
                             "SEQ_010"      =>  VARS::post("SEQ_010"),
                             "SEQ_011"      =>  VARS::post("SEQ_011"),
                             "SEQ_012"      =>  "1",
                             "REMARK1_012"  =>  VARS::post("REMARK1_012"),
                             "REMARK2_012"  =>  VARS::post("REMARK2_012"),
                             "REMARK3_012"  =>  VARS::post("REMARK3_012"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd             = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        
        if (VARS::get("mode") == "1"){
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->subclasscd);
            unset($this->leftyear);
            //呼び出し元のパラメータをセット
            $this->year      = VARS::get("SEND_YEAR");
            $this->getPrgId  = VARS::get("SEND_PRGID");
            $this->sendAuth  = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;

        $temp = VARS::get("SUBCLASSCD");
        if (isset($temp)){
            $this->leftyear       = VARS::get("YEAR");
            $this->classcd        = VARS::get("CLASSCD");
            $this->school_kind    = VARS::get("SCHOOL_KIND");
            $this->curriculum_cd  = VARS::get("CURRICULUM_CD");
            $this->subclasscd     = VARS::get("SUBCLASSCD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーの更新チェック
        if ($this->classcd == "") {
            $this->setWarning("MSG308","キー値は変更できません。");
            return false;
        }
        //キーの更新チェック
        if ($this->school_kind == "") {
            $this->setWarning("MSG308","キー値は変更できません。");
            return false;
        }
        if ($this->cmd === "update") {
            //SEQ_012
            if ($this->field["REMARK1_012"] == "1") {
                if ($this->field["REMARK2_012"] == "1" || $this->field["REMARK3_012"] == "1") {
                    $this->setWarning("MSG203","学期は複数選ぶことができません。");
                    return false;
                }
            }
            if ($this->field["REMARK2_012"] == "1") {
                if ($this->field["REMARK1_012"] == "1" || $this->field["REMARK3_012"] == "1") {
                    $this->setWarning("MSG203","学期は複数選ぶことができません。");
                    return false;
                }
            }
            if ($this->field["REMARK3_012"] == "1") {
                if ($this->field["REMARK1_012"] == "1" || $this->field["REMARK2_012"] == "1") {
                    $this->setWarning("MSG203","学期は複数選ぶことができません。");
                    return false;
                }
            }
        }
        
        $row = knjz070_3Query::getRow($this);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }
        
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
            $this->year = $this->leftyear;
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage("MSG201");
            $this->year = $this->leftyear;
        }
    }
    
    //コピー用のモデルを返します
    function &getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //去年度データ取得
        $lastYear = $db->getRow(knjz070_3Query::getDetailDat($this, "LAST"));
        //今年度データ取得
        $nowYear = $db->getRow(knjz070_3Query::getDetailDat($this, ""));
        //DB切断
        Query::dbCheckIn($db);
        //データ有無
        if (!is_array($lastYear)){
            $this->setWarning("MSG203","前年度データが、ありません。");
            return false;
        }
        //データ有無
        if (is_array($nowYear)){
            $this->setWarning("MSG203","今年度データが、存在しています。");
            return false;
        }
        $this->copy();
        $this->setMessage("MSG201");
        $this->year = $this->leftyear;
        return;
    }
    
    //コピー時の内部メソッド
    function &copy()
    {
        $result = knjz070_3Query::getCopyQuery($this);
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz070_3Query::getUpdateQuery($this->field, $this);
    }

    //削除時の内部メソッド
    function &delete()
    {
        knjz070_3Query::getDeleteQuery($this);
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
