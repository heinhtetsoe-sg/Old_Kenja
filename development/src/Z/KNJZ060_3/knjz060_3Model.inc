<?php

require_once('for_php7.php');


class knjz060_3Model extends Model {
    var $field      = array();
    var $cmd;
    var $year;
    var $leftyear = "";
    var $classcd = "";        //primary
    var $school_kind = "";

    function knjz060_3Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "CLASSCD"      =>  VARS::post("CLASSCD"),
                             "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),
                             "CLASSCD_SET"  =>  VARS::request("CLASSCD_SET"),
                             "CLASSNAME"    =>  VARS::request("CLASSNAME"),
                             
                             "SEQ_001"      =>  VARS::post("SEQ_001"),
                             "SEQ_002"      =>  VARS::post("SEQ_002"),
                             "SEQ_003"      =>  VARS::post("SEQ_003"),
                             "SEQ_004"      =>  VARS::post("SEQ_004"),
                             "REMARK1_004"  =>  VARS::post("REMARK1_004"),
                             "SEQ_005"      =>  VARS::post("SEQ_005"),
                             "REMARK1_005"  =>  VARS::post("REMARK1_005"),
                             "SEQ_006"      =>  VARS::post("SEQ_006"),
                             "SEQ_007"      =>  VARS::post("SEQ_007"),
                             "REMARK1_007"  =>  VARS::post("REMARK1_007"),
                             "REMARK2_007"  =>  VARS::post("REMARK2_007"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd             = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        if (VARS::get("mode") == "1"){
            unset($this->classcd);
            unset($this->leftyear);
            //左画面のyearをセット
            $this->year      = VARS::get("SEND_YEAR");
        }
        
        $temp = VARS::get("CLASSCD");
        if (isset($temp)){
            $this->leftyear     = VARS::get("YEAR");
            $this->classcd      = VARS::get("CLASSCD");
            $this->school_kind  = VARS::get("SCHOOL_KIND");
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
            if ($this->field["SEQ_004"]) {
                if (!$this->field["REMARK1_004"]) {
                    $this->setWarning("MSG203","出力順を指定して下さい。");
                    return false;
                }
            } else if ($this->field["REMARK1_004"]) {
                if (!$this->field["SEQ_004"]) {
                    $this->setWarning("MSG203","出力順だけを入力することはできません。");
                    return false;
                }
            }
            if ($this->field["SEQ_005"]) {
                if (!$this->field["REMARK1_005"]) {
                    $this->setWarning("MSG203","表示順を指定して下さい。");
                    return false;
                }
            } else if ($this->field["REMARK1_005"]) {
                if (!$this->field["SEQ_005"]) {
                    $this->setWarning("MSG203","表示順だけを入力することはできません。");
                    return false;
                }
            }
            if ($this->field["SEQ_007"]) {
                if (!$this->field["REMARK1_007"]) {
                    $this->setWarning("MSG203","表示順を指定して下さい。");
                    return false;
                }
            } else if ($this->field["REMARK1_007"]) {
                if (!$this->field["SEQ_007"]) {
                    $this->setWarning("MSG203","表示順だけを入力することはできません。");
                    return false;
                }
            }
        }
        
        $row = knjz060_3Query::getRow($this);
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
        $lastYear = $db->getRow(knjz060_3Query::getDetailDat($this, "LAST"));
        //今年度データ取得
        $nowYear = $db->getRow(knjz060_3Query::getDetailDat($this, ""));
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
        $result = knjz060_3Query::getCopyQuery($this);
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz060_3Query::getUpdateQuery($this->field, $this);
    }

    //削除時の内部メソッド
    function &delete()
    {
        knjz060_3Query::getDeleteQuery($this);
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
