<?php

require_once('for_php7.php');

class knjh155Model extends Model {

        var $field        = array();
        var $clear        = array();
        var $control_data = array();    //コントロールマスタデータを保持
        var $cmd;
        var $prg_first;
        var $prg;
        var $schregno;
        var $sec_competence;            //セキュリティーチェック
        var $detail_sdate;              //登録日付
        var $detail_edate;              //終了日付
        var $org_detail_sdate;
        var $org_detail_edate;
        var $detaildiv;
        var $org_detaildiv;
        var $def_detaildiv;
        var $dtclick = false;

        function knjh155Model()
        {
//              common::GetControlMaster_Fnc($this->control_data);
        }

        function init()
        {
                $this->cmd       = VARS::request("cmd");
                $this->prg_first = VARS::request("PRG");
                if ($this->prg_first){
                    $this->prg = $this->prg_first;
                }
                if ($this->cmd == ""){
                    unset($this->schregno);
                    unset($this->prg_first);
                    unset($this->prg);
                }
                if ($this->cmd == "right_list") $this->clear = array();
                if (VARS::get("DETAIL_SDATE")){
                    $this->clear = array();
                    $this->org_detaildiv = VARS::get("DETAIL_DIV");
                    $this->def_detaildiv = VARS::get("DETAIL_DIV");
                    $this->dtclick = true;
                }else {
                    $this->field = array("DETAIL_SDATE" =>  VARS::post("DETAIL_SDATE"),     //登録日付
                                         "DETAIL_EDATE" =>  VARS::post("DETAIL_EDATE"),     //終了日
                                         "DETAIL_DIV"   =>  VARS::post("DETAIL_DIV"),       //詳細区分
                                         "CONTENT"      =>  VARS::post("CONTENT"),          //賞罰内容
                                         "REMARK"       =>  VARS::post("REMARK"),           //備考
                                         "BICYCLE_CD"   =>  VARS::post("BICYCLE_CD"),       //自転車許可番号
                                         "BICYCLE_NO"   =>  VARS::post("BICYCLE_NO"),       //駐輪所番号
                                         "temp_cd"      =>  VARS::post("temp_cd"),          //最初のCD
                                         "UPDATED"      =>  VARS::post("UPDATED")
                                         );
                    $this->org_detaildiv = VARS::post("DETAIL_DIV");
                    $this->dtclick = false;
                }
                $this->getPropertiesAll();
                if ($this->Properties["use_prg_schoolkind"] == "1") {
                    $this->getSelectSchoolKind();
                }

                if (VARS::post("DETAIL_DIV")!="") $this->detaildiv = VARS::post("DETAIL_DIV");
                if (VARS::get("DETAIL_DIV")!="") $this->detaildiv = VARS::get("DETAIL_DIV");

//              $this->schregno = VARS::request("SCHREGNO");

                if (VARS::get("SCHREGNO")){
                    $this->schregno = VARS::get("SCHREGNO");
                }
//              if (VARS::request("SCHREGNO")=="") $this->schregno  = VARS::post("SCHREGNO");

                if (VARS::get("DETAIL_SDATE")){
                    $this->detail_sdate = VARS::get("DETAIL_SDATE");
                }else {
                    $this->detail_sdate = VARS::post("DETAIL_SDATE");
                }
                if (VARS::get("DETAIL_EDATE")){
                    $this->detail_edate = VARS::get("DETAIL_EDATE");
                }else {
                    $this->detail_edate = VARS::post("DETAIL_EDATE");
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
                
                $this->org_detail_sdate = VARS::get("DETAIL_SDATE");
                $this->org_detail_edate = VARS::get("DETAIL_EDATE");
                
                if (VARS::post("org_detail_sdate")) {
                    $this->org_detail_sdate = VARS::post("org_detail_sdate");
                }
                
                if (VARS::post("org_detail_edate")) {
                    $this->org_detail_edate = VARS::post("org_detail_edate");
                }
                if ($this->cmd=="clear") {
                     $this->detail_sdate = $this->org_detail_sdate;
                     $this->detail_edate = $this->org_detail_edate;
                }
        }

        //入力項目チェック
        function validateInput($kind)
        {
            $this->schregno = VARS::request("SCHREGNO");
            //日付が不正かどうかをチェック
            if ($kind!="delete" && ($this->field["DETAIL_SDATE"]=="" || $this->field["DETAIL_EDATE"]=="" || $this->field["BICYCLE_CD"]=="")) {
                    $this->setWarning("MSG301");
                    return false;
            }
            if ($this->schregno=="") {
                $this->schregno."==";
                $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
                return false;
            }

            //１レコード取得
            $row = knjh155Query::getRow($this->field["DETAIL_EDATE"],$this->field["DETAIL_SDATE"],$this->schregno,$this->field["DETAIL_DIV"]);
            $this->isload = "0";

            if ($kind=="update" || $kind=="delete") {
                    //リストより選択していない
                    if ($this->field["temp_cd"]==""){
                            $this->setWarning("MSG308");
                            return false;
                    }
                    //登録日付が変更されていますかをチェック
                    if ($row["DETAIL_SDATE"] != $this->org_detail_sdate ||
                        ($this->field["DETAIL_DIV"] == 3 && $row["DETAIL_EDATE"] != $this->org_detail_edate)){
                            $this->isload = "1";
                            $this->setWarning("MSG308","キー値が変更されています。");
                            return false;
                    }
                    //キー値が変更されていますかをチェック
                    if ($row["SCHREGNO"] != $this->schregno ||
                        $row["DETAIL_DIV"] != $this->def_detaildiv ){
                            $this->isload = "1";
                            $this->setWarning("MSG308","キー値が変更されています。");
                            return false;
                    }
            }

            if ($kind=="update" || $kind=="insert") {
                    $e_date_f = common::DateConv1($this->field["DETAIL_EDATE"],12);
                    if (str_replace("/","",$this->field["DETAIL_SDATE"]) > str_replace("/","",$this->field["DETAIL_EDATE"])){
                            $this->setWarning("MB0002","開始日付と終了日付の\\n大小が逆です。");
                            return false;
                    }
                    //重複チェック
                    if ($this->cmd == "add" && is_array($row)){
                            $this->setWarning("MSG302");
                            return false;
                    }
                    if (strlen($this->field["BICYCLE_NO"]) > 4) {
                            $this->setWarning("MSG915","(駐輪場番号は4バイトまでです)");
                            return false;
                    }
                    if (strlen($this->field["CONTENT"]) >120) {
                            $this->setWarning("MSG915","(詳細内容は120バイトまでです)");
                            return false;
                    }
                    if (strlen($this->field["REMARK"]) > 60) {
                            $this->setWarning("MSG915","(備考は60バイトまでです)");
                            return false;
                    }
            }
            return true;
        }

        //編集完了用のモデルを返します
        function &getUpdateModel()
        {
                //入力チェック
                if ($this->validateInput("update")){
                        $this->update();
                }
        }

        function &getInsertModel()
        {
                //入力チェック
                if ($this->validateInput("insert")){
                        $this->org_detail_sdate = $this->field["DETAIL_SDATE"];
                        $this->insert();
                }
                return;
        }

        //削除用のモデルを返します
        function &getDeleteModel()
        {
                //入力チェック
                if ($this->validateInput("delete")){
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
                $result = knjh155Query::getDeleteQuery($this->field,$this->schregno);
                if ( !isset($this->warning)){
                        $this->setMessage("MSG201");
                }
                $this->field = array();
                $this->detail_sdate = "";   //NO002
                $this->detail_edate = "";   //NO002
                return $result;
        }

        //インサート時の内部メソッド
        function &insert()
        {
                $result = knjh155Query::getInsertQuery($this->field,$this->schregno);
                if ( !isset($this->warning)){
                        $this->setMessage("MSG201");
                }
                return $result;
        }

        //アップデート時の内部メソッド
        function &update()
        {
                $result = knjh155Query::getUpdateQuery($this->field,$this->schregno,$this->org_detail_sdate);
                if ( !isset($this->warning)){
                        $this->setMessage("MSG201");
                }
                return $result;
        }
}
?>
