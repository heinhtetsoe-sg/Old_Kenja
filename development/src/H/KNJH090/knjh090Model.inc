<?php

require_once('for_php7.php');


class knjh090Model extends Model
{
    public $field        = array();
    public $clear        = array();
    public $cmd;
    public $schregno;
    public $year;
    public $control_data = array();    //コントロールマスタデータを保持
    public $sec_competence;            //セキュリティーチェック
    public $detail_sdate;              //登録日付 NO001
    public $detail_edate;              //終了日付 NO001
    public $org_detail_sdate;          //NO001
    public $org_detail_edate;          //NO001
    public $detaildiv;                 //NO001
    public $org_detaildiv;             //NO001
    public $def_detaildiv;             //NO001
    public $dtclick = false;           //NO001
    public $occurrence_date;           //発生日付
    public $investigation_date;        //調査日付
    public $std_guid_mtg_date;         //生徒指導部会議
    public $staff_mtg_date;            //職員会議

    public function knjh090Model()
    {
    }

    public function init()
    {
        $this->cmd       = VARS::request("cmd");
        if ($this->cmd == "right_list") {
            $this->clear = array();
        }
        if (VARS::get("DETAIL_SDATE")) {
            $this->clear = array();
            $this->org_detaildiv = VARS::get("DETAIL_DIV");     //NO001
            $this->def_detaildiv = VARS::get("DETAIL_DIV");     //NO001
            $this->dtclick = true;
        } else {
            $this->field = array("DETAIL_SDATE"  =>  VARS::post("DETAIL_SDATE"),        //登録日付 NO001
                                 "DETAIL_EDATE"  =>  VARS::post("DETAIL_EDATE"),        //終了日   NO001
                                 "DETAIL_DIV"    =>  VARS::post("DETAIL_DIV"),          //詳細区分 NO001
                                 "DETAILCD"      =>  VARS::post("DETAILCD"),            //罰則     NO001
                                 "CONTENT"       =>  VARS::post("CONTENT"),             //賞罰内容
                                 "REMARK"        =>  VARS::post("REMARK"),              //備考
                                 "BICYCLE_CD"    =>  VARS::post("BICYCLE_CD"),          //自転車許可番号
                                 "BICYCLE_NO"    =>  VARS::post("BICYCLE_NO"),          //駐輪所番号
                                 "temp_cd"       =>  VARS::post("temp_cd"),             //最初のCD
                                 "UPDATED"       =>  VARS::post("UPDATED"),
                                 "OCCURRENCE_DATE"        =>  VARS::post("OCCURRENCE_DATE"),        //発生日付
                                 "INVESTIGATION_DATE"     =>  VARS::post("INVESTIGATION_DATE"),     //調査日付
                                 "STD_GUID_MTG_DATE"      =>  VARS::post("STD_GUID_MTG_DATE"),      //生徒指導部会議
                                 "ORIGINAL_PLAN_CD"       =>  VARS::post("ORIGINAL_PLAN_CD"),       //原案
                                 "STAFF_MTG_DATE"         =>  VARS::post("STAFF_MTG_DATE"),         //職員会議
                                 "PUNISH_CD"              =>  VARS::post("PUNISH_CD"),              //処分
                                 "OCCURRENCE_PLACE"       =>  VARS::post("OCCURRENCE_PLACE"),       //場所
                                 "DIARY_FLG"              =>  VARS::post("DIARY_FLG"),              //日誌
                                 "WRITTEN_OATH_FLG"       =>  VARS::post("WRITTEN_OATH_FLG"),       //誓約書
                                 "REPORT_FLG"             =>  VARS::post("REPORT_FLG"),             //調書
                                 "WRITTEN_STAFFCD"        =>  VARS::post("WRITTEN_STAFFCD"),        //資料作成者
                                 "INVESTIGATION_STAFFCD1" =>  VARS::post("INVESTIGATION_STAFFCD1"), //調査委員1
                                 "INVESTIGATION_STAFFCD2" =>  VARS::post("INVESTIGATION_STAFFCD2"), //調査委員2
                                 "INVESTIGATION_STAFFCD3" =>  VARS::post("INVESTIGATION_STAFFCD3"), //調査委員3
                                 "INVESTIGATION_STAFFCD4" =>  VARS::post("INVESTIGATION_STAFFCD4")  //調査委員4
                                 );
            $this->org_detaildiv = VARS::post("DETAIL_DIV");
            $this->dtclick = false;
        }
        if (VARS::post("DETAIL_DIV")!="") {
            $this->detaildiv = VARS::post("DETAIL_DIV");
        }      //NO001
        if (VARS::get("DETAIL_DIV")!="") {
            $this->detaildiv = VARS::get("DETAIL_DIV");
        }        //NO001

        $this->schregno   = VARS::request("SCHREGNO");

        if (VARS::request("SCHREGNO")=="") {
            $this->schregno = VARS::post("SCHREGNO");
        }
        if (VARS::get("YEAR")        !="") {
            $this->year     = VARS::get("YEAR");
        }

        if (VARS::get("DETAIL_SDATE")) {
            $this->detail_sdate = VARS::get("DETAIL_SDATE");  //NO001
        } else {
            $this->detail_sdate = VARS::post("DETAIL_SDATE"); //NO001
        }
        if (VARS::get("DETAIL_EDATE")) {
            $this->detail_edate = VARS::get("DETAIL_EDATE");  //NO001
        } else {
            $this->detail_edate = VARS::post("DETAIL_EDATE"); //NO001
        }

        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS")) {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO")) {
            $this->attendno = VARS::get("ATTENDNO");
        }

        $this->org_detail_sdate = VARS::get("DETAIL_SDATE");    //NO001
        $this->org_detail_edate = VARS::get("DETAIL_EDATE");    //NO001

        if (VARS::post("org_detail_sdate")) {   //NO001
            $this->org_detail_sdate = VARS::post("org_detail_sdate");
        }

        if (VARS::post("org_detail_edate")) {   //NO001
            $this->org_detail_edate = VARS::post("org_detail_edate");
        }
        if ($this->cmd=="clear") {
            $this->detail_sdate = $this->org_detail_sdate;
            $this->detail_edate = $this->org_detail_edate;
        }

        $db = Query::dbCheckOut();
        $query = knjh090Query::getTokiwagiChkInfo();
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->chktokiwagiflg = $row['NAME1'] === "tokiwagi" ? true : false;
        }

        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //日付が不正かどうかをチェック
        if ($kind!="delete" && ($this->field["DETAIL_SDATE"]=="" || ($this->field["DETAIL_DIV"] == 3 && $this->field["DETAIL_EDATE"]==""))) {   //NO001
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->schregno=="") {
            $this->schregno."==";
            $this->setWarning("MSG304", "(左より生徒を選択してから行ってください)");
            return false;
        }

        //罰データで発生日付がない場合、エラー
        if ($this->field["DETAIL_DIV"] == '2' && $model->Properties["KNJH090DETAIL2PATAN"] == '1' && $this->field["OCCURRENCE_DATE"]=="") {
            $this->setWarning("MSG301");
            return false;
        }

        //１レコード取得
        $setYear = $this->year ? $this->year : CTRL_YEAR;
        $row = knjh090Query::getRow($this->field["DETAIL_EDATE"], $this->field["DETAIL_SDATE"], $this->schregno, $this->field["DETAIL_DIV"], $setYear);  //NO001
        $this->isload = "0";

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->field["temp_cd"]=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //登録日付が変更されていますかをチェック NO001
            if ($row["DETAIL_SDATE"] != $this->org_detail_sdate || ($this->field["DETAIL_DIV"] == 3 && $row["DETAIL_EDATE"] != $this->org_detail_edate)) {
                $this->isload = "1";
                $this->setWarning("MSG308", "キー値が変更されています。");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($row["SCHREGNO"] != $this->schregno || $row["DETAIL_DIV"] != $this->def_detaildiv) {
                $this->isload = "1";
                $this->setWarning("MSG308", "キー値が変更されています。");
                return false;
            }
        }

        if ($kind=="update" || $kind=="insert") {
            //NO001
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG302");
                return false;
            }
            if (strlen($this->field["OCCURRENCE_PLACE"]) >60) {
                $this->setWarning("MSG915", "(場所は60バイトまでです)");
                return false;
            }
            if (strlen($this->field["CONTENT"]) >900) {
                $this->setWarning("MSG915", "(詳細内容は900バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK"]) >60) {
                $this->setWarning("MSG915", "(備考は60バイトまでです)");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    public function &getInsertModel()
    {
        //入力チェック NO001
        if ($this->validateInput("insert")) {
            $this->org_detail_sdate = $this->field["DETAIL_SDATE"];
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }

    //デリート時の内部メソッド
    public function &delete()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        $result = knjh090Query::getDeleteQuery($this->field, $this->schregno, $this->year);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();
        $today = getdate();
        $date  = mktime(0, 0, 0, $today["mon"], $today["mday"], $today["year"]);
        $date  = date("Y-m-d", $date);

        $this->detail_sdate = $date;    //NO001
        $this->detail_edate = CTRL_DATE;    //NO001
        return $result;
    }

    //インサート時の内部メソッド
    public function &insert()
    {
        $result = knjh090Query::getInsertQuery($this->field, $this->schregno);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        //NO001
        $result = knjh090Query::getUpdateQuery($this->field, $this->schregno, $this->org_detail_sdate, $this->year);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $extension = $setFileName[get_count($setFileName) - 1];
        //拡張子チェック
        if ($extension != "pdf" && $extension != "PDF") {
            $this->warning = "PDFファイルを指定して下さい。\\n(".$extension."ファイルの取込は出来ません。)";
            return false;
        }
        //アップロード(PDF)
        $dirname1  = DOCUMENTROOT . "/pdf/" . $this->schregno;
        $dirname2  = DOCUMENTROOT . "/pdf/" . $this->schregno . "/" . $this->Properties["savePdfFolderH090"];
        $setYear = $this->year ? $this->year : CTRL_YEAR;
        $saveFileName = $setYear.$this->field["DETAIL_DIV"].str_replace("/", "", str_replace("-", "", $this->field["DETAIL_SDATE"]));

        $filename = $dirname2 . "/" . $setFileName[0] . "_" . $saveFileName . "_" . date("Ymj_His") . '.' . $extension;
        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
        } elseif ($this->schregno == "") {
            $this->warning = "学籍番号が不正です。";
        } else {
            //ディレクトリ（学籍番号）か・・・なければ作成
            if (!is_dir($dirname1)) {
                if (!mkdir($dirname1)) {
                    //作成失敗
                }
            }
            if (!is_dir($dirname2)) {
                if (!mkdir($dirname2)) {
                    //作成失敗
                }
            }
            if (move_uploaded_file($this->file['tmp_name'], $filename) == false) {
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
    }
}
