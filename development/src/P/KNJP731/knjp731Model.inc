<?php

require_once('for_php7.php');


class knjp731Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt = array(); // セレクトデータ
    var $selectleft;

    function knjp731Model() {
    }

    function init() {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         // 学期コード
                             "COURSECD"         =>  VARS::post("COURSECD"),         //課程(1:全日制、2:定時・通信制)
                             "UPDATE_DIV"       =>  VARS::post("UPDATE_DIV"),       // 更新区分
                             "RENBAN1"          =>  VARS::post("RENBAN1"),          // 新規連番
                             "RENBAN2"          =>  VARS::post("RENBAN2"),          // 追加連番
                             "OUTPUT_DATA"      =>  VARS::post("OUTPUT_DATA"),      // 出力区分
                             "KUBUN"            =>  VARS::post("KUBUN"),            // 印刷指定
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")    // 年組
                     );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;
        $this->month = VARS::post("MONTH");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class' && $this->cmd != 'csv') $this->select_opt = array();

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knjp731query::getNameMstZ010());
        Query::dbCheckIn($db);
    }

    // ＣＳＶ出力
    function getDownloadModel() {
        $header = array();
        //全日制
        if ($this->field["COURSECD"] == "1") {
            $header["RENBAN"]           = "通し番号";
            $header["NAME"]             = "生徒氏名";
            $header["NAME_KANA"]        = "ふりがな";
            $header["BIRTHDAY"]         = "生年月日";
            $header["ADDR"]             = "住所";
            $header["BEGIN_YEARMONTH"]  = "開始年月";
            $header["SCHOOL_FEES"]      = "授業料（月額）";
            $header["DECISION_MONEY"]   = "減免額（月額）";
            $header["SUP_DURING"]       = "支給限度期間";
            $header["INTENTION_FLG"]    = "申請しない人（希望しない人）";
            $header["G_CHECK1"]         = "保護者１の所得確認";
            $header["REDUC_INCOME1"]    = "保護者１の市町村所得割";
            $header["G_CHECK2"]         = "保護者２の所得確認";
            $header["REDUC_INCOME2"]    = "保護者２の市町村所得割";
            $header["REMARK"]           = "備考";

        //定時・通信制
        } else if ($this->field["COURSECD"] == "2") {
            $header["RENBAN"]           = "通し番号";
            $header["NAME"]             = "生徒氏名";
            $header["NAME_KANA"]        = "ふりがな";
            $header["BIRTHDAY"]         = "生年月日";
            $header["ADDR"]             = "住所";
            $header["BEGIN_YEARMONTH"]  = "開始年月";
            $header["SCHOOL_FEES"]      = "授業料（単位あたり）";
            $header["DECISION_MONEY"]   = "減免額（単位あたり）";
            $header["SUP_DURING"]       = "支給限度期間";
            $header["MAX_CREDIT"]       = "支給限度単位";
            $header["INTENTION_FLG"]    = "申請しない人（希望しない人）";
            $header["G_CHECK1"]         = "保護者１の所得確認";
            $header["REDUC_INCOME1"]    = "保護者１の市町村所得割";
            $header["G_CHECK2"]         = "保護者２の所得確認";
            $header["REDUC_INCOME2"]    = "保護者２の市町村所得割";
            $header["REMARK"]           = "備考";
            $header["GRP_NAME"]         = "グループ名";
            $header["CREDIT"]           = "単位数";
            $header["BEGIN_YEARMONTH2"] = "開始年月";
            $header["RISYU_KIKAN"]      = "履修期間";
            $header["GRP_REMARK"]       = "備考";
        }

        //福井県
        if ($this->schoolName == "fukui") {
            $header["GRADE"]                            = "学年";
            $header["ZIPCD"]                            = "郵便番号";
            $header["ADDR1"]                            = "住所１";
            $header["ADDR2"]                            = "住所２";
            $header["SCHOLARSHIP_PAYMENT_YES_NO_FLG"]   = "就学給付金の申請書有無";
            $header["GUARD_NAME"]                       = "保護者氏名";
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this->header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        //生徒単位の配列にする
        $arr_selectleft = explode(",", $this->selectleft);
        $arr_schregNo = array();
        if ($this->field["KUBUN"] == 1) {
            foreach ($arr_selectleft as $key => $val) {
                $studentQuery = knjp731Query::getSchno($this, $val);
                $result = $db->query($studentQuery);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $arr_schregNo[] = $row["SCHREGNO"];
                }
            }
        } else {
            foreach ($arr_selectleft as $key => $val) {
                $tmp_selectleft = explode("-", $val);
                $arr_schregNo[] = $tmp_selectleft[0];
            }
        }

        //データを更新する。
        if ($this->field["UPDATE_DIV"] == 1) {
            $maxRenban = $this->field["RENBAN1"];
        } else if ($this->field["UPDATE_DIV"] == 2) {
            $maxRenban = $this->field["RENBAN2"];
        }

        $setRendban = $maxRenban;
        foreach ($arr_schregNo as $key => $val) {
            if ($this->field["UPDATE_DIV"] != 3) {
                if ($this->field["UPDATE_DIV"] == 1) {
                    $query = knjp731Query::deleteSql($this, $val);
                    $db->query($query);
                }
                //データ作成
                $query = knjp731Query::reductionAuthorizeDatIns($this, $val, $setRendban);
                $db->query($query);
            }

            $query = knjp731Query::selectCsvData($this, $val);
            $csvRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($this->field["UPDATE_DIV"] != 3) {
                $csvRow["RENBAN"] = sprintf('%04d', $setRendban);
            }
            $csvRow["BIRTHDAY"] = str_replace("-", "/", $csvRow["BIRTHDAY"]);
            $csvRow = str_replace(array("\r\n", "\r", "\n"), "", str_replace(",",".",$csvRow));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $csvRow = str_replace(",", ".", $csvRow);

            //ヘッダ順に値をセット
            $comma = "";
            foreach ($header as $key => $headerName) {
                $contents .= $comma.$csvRow[$key];
                $comma = ",";
            }
            $contents .= "\n";
            $this->contents = $contents;

            $this->header_data = true;
            $setRendban++;
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->cmd = "updateEnd";
        return true;
    }

    function outPutCsv() {
        // ＣＳＶ出力
        if($this->header_data) {
            $setHeaderName = ($this->field["COURSECD"] == "1") ? "全日制": "定時・通信制";
            $filename = sprintf("%s.csv", $setHeaderName."_受給資格認定申請者入力対応用");
            common::downloadContents($this->contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
