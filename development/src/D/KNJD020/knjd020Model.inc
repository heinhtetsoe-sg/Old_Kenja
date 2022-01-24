<?php

require_once('for_php7.php');

class knjd020Model extends Model {
    var $field      = array();
    var $new_field  = array();
    var $cmd;
    var $term_err_flg = "0"; //実施期間外エラーフラグ
    var $delete_flg = "0"; //削除前処理チェックフラグ
    var $s_id;
    var $sort;
        
    function knjd020Model()
    {
    }
    
    function init()
    {               
        if (VARS::get("PROGRAMID") != "") {
            //起動時のデフォルトでソートを講座コードの昇順にする
            $this->s_id = "1";
            $this->sort["1"] = "1";
            $this->sort["2"] = "0";
        } elseif (VARS::get("sort1") != "" || VARS::get("sort2") != "") {
            $this->sort["1"] = (VARS::get("sort1") != "" ) ? VARS::get("sort1") : "0";
            $this->sort["2"] = (VARS::get("sort2") != "" ) ? VARS::get("sort2") : "0";
        }

        if (VARS::get("s_id") != ""){
            $this->s_id     = VARS::get("s_id");
        }
        
        $this->term_err_flg = "0";
        $this->delete_flg = "0";
        $this->cmd = VARS::request("cmd");
        
        if (VARS::post("GTREDATA")!="") $this->field = unserialize(VARS::post("GTREDATA"));
        if (VARS::get("CHAIRCD")!="")   $this->field["chairSel"] = VARS::get("CHAIRCD");
        if (VARS::get("GROUPCD")!="")   $this->field["groupcd"] = VARS::get("GROUPCD");
        if (VARS::get("TESTCD")!="")    $this->field["testcd"] = VARS::get("TESTCD");
        if (VARS::get("PERFECT")!="")   $this->field["perfect"] = VARS::get("PERFECT");

        $this->new_field = array("TESTCD"           => VARS::post("TESTCD"),
                                 "OPERATION_DATE"   => VARS::post("OPERATION_DATE"),
                                 "PERFECT"          => VARS::post("PERFECT"),
                                 "RATE"             => VARS::post("RATE"),
                                 "ALL"              => VARS::post("ALL"),
                                 "UPDATED"          => VARS::post("UPDATED"));
        
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
            $this->new_field = array();
        }
        //左のリストから選択されたとき初期化
        if(VARS::get("SHOW_FLG")=="show") {
            $this->new_field = array();
            $this->field["testcd"] = "";
        }
    }

    function validateInput($process_flg)
    {   
        ///項目の値の有無のチェックのみだったのでJSファイルに移した
    }
    
    //実施日が実施期間内かチェック
    //引数のCMDの値は最初はこのメソッドにくるためにaddまたはupdateに_checkをつけて区別する。
    //それを取り除いて再度cmdの値として保持する
    function &getTermCheckModel($cmd)
    {
        if ($cmd == "update_check") $this->new_field["ALL"] = 0;   
        if ($this->new_field["ALL"] !=1) {
            $row = knjd020Query::GetStartDate($this->field["chairSel"]);
            if (strtotime(str_replace("-","/",$row["STARTDAY"])) > strtotime($this->new_field["OPERATION_DATE"]) ||
                strtotime($this->new_field["OPERATION_DATE"]) > strtotime(str_replace("-","/",$row["ENDDAY"])) ){
                $this->term_err_flg = str_replace("_check","",$cmd);
            }
        //全てに適応チェックボックスの時
        } else {
            $num = 0;
            $row = knjd020Query::GetStartDate($this->field["CHAIRCD"]);
            foreach ($this->field["CHAIRCD"] as $val) {
                if (strtotime(str_replace("-","/",$row[$val]["STARTDAY"])) > strtotime($this->new_field["OPERATION_DATE"]) ||
                    strtotime($this->new_field["OPERATION_DATE"]) > strtotime(str_replace("-","/",$row[$val]["ENDDAY"])) ){
                    $this->term_err_flg = str_replace("_check","",$cmd);
                }
            }
        }
        //実施期間内であればそのまま処理を続ける
        if ($this->term_err_flg == "0") {
            if ($cmd == "update_check") $this->getUpdateModel();
            if ($cmd == "add_check") $this->getInsertModel();
        }
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->field["testcd"]=="") {
            $this->setWarning("MSG308");
            return false;
        }
        
        //テスト項目を変更する場合は重複チェック
        if ($this->field["testcd"]!=$this->new_field["TESTCD"]) {
            
            if (knjd020Query::RecordCheck2($this) != "0"){
                $this->setWarning("MSG302");
                return false;
            }
            
            //TESTSCORE_DATにデータがある場合はエラー
            if (knjd020Query::getTestscoreCount($this,$this->field["testcd"])!="0") {
                $this->setWarning("MSG300","既に得点入力が行われています");
                return false;
            }
        }
        
        //満点を変更する場合
        if ($this->field["perfect"]!=$this->new_field["PERFECT"]) {
            $count = knjd020Query::getTestscoreCount($this,$this->field["testcd"],1);
            if ($count!="0") {
                $this->setWarning("MSG300","満点以上の生徒が ".$count." 名存在します。");
                return false;
            }
        }

        knjd020Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        $this->field["testcd"] = $this->new_field["TESTCD"];
    }

    //追加処理
    function &getInsertModel()
    {
        //該当データの有無をチェック 
        if (knjd020Query::RecordCheck2($this) == "0") {
            knjd020Query::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->field["testcd"] = $this->new_field["TESTCD"];
        } else {
            $this->setWarning("MSG302");
        }
    }
    //削除処理
    function &getDeleteModel()
    {
        knjd020Query::getDeleteQuery($this);
        $this->setMessage("MSG201");
        $this->field["chairSel"]="";
        $this->field["testcd"]="";
        $this->new_field = array();
    }
    
    //削除前にTESTSCORE_DATに削除対象テストのデータをチェックする
    function &getDeleteCheckModel()
    {
        //データがある場合は戻る
        if (knjd020Query::getTestscoreCount($this,$this->new_field["TESTCD"]) !="0") {
            $this->delete_flg = "1";
        
        //対象データがない場合は処理を続ける
        } else {
            $this->getDeleteModel();
        }
    }
}
?>
