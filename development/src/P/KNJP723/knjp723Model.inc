<?php

require_once('for_php7.php');


class knjp723Model extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $hr_class;
    var $data_cnt;
    var $control;
    var $z010name1;

    function knjp723Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //入力項目
        $this->fieldList = array("PASSNO",
                                 "INTENTION_YES_FLG",
                                 "INTENTION_NO_FLG",
                                 "FORMS_YES_FLG",
                                 "FORMS_NO_FLG",
                                 "FATHER_TAX_CERTIFICATE_FLG",
                                 "FATHER_SPECIAL_TAX_DEC_FLG",
                                 "FATHER_TAX_NOTICE_FLG",
                                 "MOTHER_TAX_CERTIFICATE_FLG",
                                 "MOTHER_SPECIAL_TAX_DEC_FLG",
                                 "MOTHER_TAX_NOTICE_FLG",
                                 "MAINTAINER_TAX_CERTIFICATE_FLG",
                                 "MAINTAINER_SPECIAL_TAX_DEC_FLG",
                                 "MAINTAINER_TAX_NOTICE_FLG",
                                 "SCHOLARSHIP_PAYMENT_YES_NO_FLG",
                                 "REMARK"
                                );

        $this->year = VARS::post("YEAR");
        $this->semester = VARS::post("SEMESTER");
        $this->month = VARS::post("MONTH");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;
        $this->grade = VARS::post("GRADE");
        $this->hr_class = VARS::post("HR_CLASS");
        $this->sort = VARS::post("SORT");

        $this->cmd   = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        foreach ($this->setList as $counter => $Row) {
            $setKey = $Row["SCHREGNO"];
            //バイト数チェック
            if (strlen($this->field[$setKey]["PASSNO"]) > 19) {
                $this->setWarning("MSG915","(資格認定番号は19バイトまでです。)");
                return false;
            }
            if (strlen($this->field[$setKey]["REMARK"]) > 45) {
                $this->setWarning("MSG915","(備考は45バイトまでです。)");
                return false;
            }
        } //foreach

        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        $this->field = array();
        foreach ($this->setList as $counter => $Row) {
            $setKey = $Row["SCHREGNO"];
            foreach ($this->fieldList as $fieldName) {
                $this->field[$setKey][$fieldName] = VARS::post("{$fieldName}-{$setKey}");
            }
        } //foreach

        //入力チェック
        if ($this->validateInput()) {
            knjp723Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $contents  = "";    //クリア
        $header_data = false;   //出力フラグ

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ一覧
        $header = array();
        $header["ATTENDNO"]                         = "年組番";
        $header["SCHREGNO"]                         = "学籍番号";
        $header["NAME"]                             = "氏名";
        $header["NAME_KANA"]                        = "ふりがな";
        $header["PASSNO"]                           = "資格認定番号";
        $header["INTENTION_YES_FLG"]                = "申請意思有";
        $header["INTENTION_NO_FLG"]                 = "申請意思無";
        $header["FORMS_YES_FLG"]                    = "申請書有";
        $header["FORMS_NO_FLG"]                     = "申請書無";
        $header["FATHER_TAX_CERTIFICATE_FLG"]       = "添付書類/保護者（父）/課税証明書";
        $header["FATHER_SPECIAL_TAX_DEC_FLG"]       = "添付書類/保護者（父）/特別徴収税額決定通知書";
        $header["FATHER_TAX_NOTICE_FLG"]            = "添付書類/保護者（父）/納税通知書";
        $header["MOTHER_TAX_CERTIFICATE_FLG"]       = "添付書類/保護者（母）/課税証明書";
        $header["MOTHER_SPECIAL_TAX_DEC_FLG"]       = "添付書類/保護者（母）/特別徴収税額決定通知書";
        $header["MOTHER_TAX_NOTICE_FLG"]            = "添付書類/保護者（母）/納税通知書";
        $header["MAINTAINER_TAX_CERTIFICATE_FLG"]   = "添付書類/主たる生計維持者/課税証明書";
        $header["MAINTAINER_SPECIAL_TAX_DEC_FLG"]   = "添付書類/主たる生計維持者/特別徴収税額決定通知書";
        $header["MAINTAINER_TAX_NOTICE_FLG"]        = "添付書類/主たる生計維持者/納税通知書";
        $header["SCHOLARSHIP_PAYMENT_YES_NO_FLG"]   = "奨学給付金事前申込書の有無";
        $header["REMARK"]                           = "備考";

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ出力
        $query = knjp723Query::getSchregList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番";

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = CTRL_YEAR."年度　就学支援金受給資格認定申請確認";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
