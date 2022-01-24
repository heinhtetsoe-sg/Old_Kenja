<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjp851Model extends Model
{
    public $cmd;
    public $control;
    public $file;
    public $field = array();
    public $data_arr = array();

    /**
     * メンバ変数の初期化
     */
    public function knjp851Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->ctrlYear     = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->schoolcd = sprintf("%012d", SCHOOLCD);

        $this->maxSeq = 2;
        $this->cmd = VARS::request("cmd");
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //対象年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),         //クラス
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "PATTERN_DIV"      =>  VARS::post("PATTERN_DIV"),      //対象種別
                             "DATE_DIV"         =>  VARS::post("DATE_DIV"),         //処理名
                             "DATE_CHK1"        =>  VARS::post("DATE_CHK1"),        //未納のみ出力(徴収日)
                             "DATE_CHK2"        =>  VARS::post("DATE_CHK2"),        //対象年度以外の入金を含む(入金日)
                             "SUM_CHK"          =>  VARS::post("SUM_CHK"),          //合計出力
                             "MONTH_CHK_ALL"    =>  VARS::post("MONTH_CHK_ALL"),    //月全選択
                             "IN_OUT_DIV"       =>  VARS::post("IN_OUT_DIV"),       //収入支出
                             );

        $this->selectedMonth = array();
        if (VARS::post("SELECTED_MONTH") != "") {
            $this->selectedMonth = explode(",", VARS::post("SELECTED_MONTH"));
        }
        $this->getPropertiesAll();
    }

    //CSV出力(データ)
    public function OutputDataFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        $header_data = false;

        //ヘッダ
        $header   = $this->getHeadArray($db);
        $contents .= implode(",", $header);
        $contents .= "\n";

        //SQL文発行
        if ($this->field["PATTERN_DIV"] == "1") {
            //入金計画・実績情報
            $query = knjp851Query::getSelectPlanPaidQuery($this);
        } elseif ($this->field["PATTERN_DIV"] == "2") {
            //減免情報
            $query = knjp851Query::getSelectReductionQuery($this);
        } elseif ($this->field["PATTERN_DIV"] == "3") {
            //寮情報
            $query = knjp851Query::getSelectDomitoryQuery($this);
        } else {
            //学校徴収金（預り金）情報
            if ($this->field["IN_OUT_DIV"] == "1") {
                $query = knjp851Query::getSelectLevyIncomeQuery($this);
            } else {
                $query = knjp851Query::getSelectLevyOutgoQuery($this);
            }
        }

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);

            //前処理
            $this->preprocess($row);
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, "入金計画・実績情報.csv");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function preprocess(&$row)
    {
        //学年・組名
        $tmp = explode("-", $row["HR_NAME"]);
        $row["GRADE_NAME"]    = (count($tmp) == 2) ? $tmp[0] : "";
        $row["HR_CLASS_NAME"] = (count($tmp) == 2) ? $tmp[1] : "";

        //対象種別ごと
        if ($this->field["PATTERN_DIV"] == "1") {
            $row["DIV_NO"]          = "10";
            $row["PLAN_YEAR_MONTH"] = str_replace("-", "/", $row["PLAN_YEAR_MONTH"]);
            $row["SLIP_NO"]         = ($row["SLIP_NO"] != "") ? $row["SLIP_NO"]."'" : "";
        } elseif ($this->field["PATTERN_DIV"] == "2") {
            $row["DIV_NO"] = "20";
        } elseif ($this->field["PATTERN_DIV"] == "3") {
            $row["DIV_NO"] = "30";
        } else {
            if ($this->field["IN_OUT_DIV"] == "1") {
                $row["DIV_NO"]          = "40";
                $row["IN_OUT_DIV"]      = "01";
                $row["IN_OUT_DIV_NAME"] = "収入";
                $row["INCOME_APPROVAL"] = ($row["INCOME_APPROVAL"] != "") ? sprintf("%02d", $row["INCOME_APPROVAL"]) : "";
                $row["INCOME_APPROVAL_NAME"] = ($row["INCOME_APPROVAL"] != "") ? "決裁済み" : "";
            } else {
                $row["DIV_NO"]          = "50";
                $row["IN_OUT_DIV"]      = "02";
                $row["IN_OUT_DIV_NAME"] = "支出";
                $row["OUTGO_APPROVAL"]  = ($row["OUTGO_APPROVAL"] != "") ? sprintf("%02d", $row["OUTGO_APPROVAL"]) : "";
                $row["OUTGO_APPROVAL_NAME"] = ($row["OUTGO_APPROVAL"] != "") ? "決裁済み" : "";
            }
        }
    }

    public function getHeadArray($db)
    {
        $retHeadArray = array();
        $retHeadArray["SCHREGNO"]           = "学籍番号";
        $retHeadArray["SCHOOL_KIND_NAME"]   = "学校区分";
        $retHeadArray["GRADE_NAME"]         = "学年";
        $retHeadArray["HR_CLASS_NAME"]      = "組";
        $retHeadArray["ATTENDNO"]           = "出席番号";
        $retHeadArray["NAME"]               = "生徒氏名";
        $retHeadArray["NAME_KANA"]          = "生徒氏名かな";
        $retHeadArray["CLUBNAME1"]          = "所属クラブ";
        $retHeadArray["CLUBNAME2"]          = "所属クラブ";
        $retHeadArray["KOJIN_NO"]           = "個人番号";
        $retHeadArray["GRD_DATE"]           = "転退学日";
        $retHeadArray["DIV_NO"]             = "区分";
        if ($this->field["PATTERN_DIV"] == "1") {
            $retHeadArray["DISP_YEAR"]                  = "年度";
            $retHeadArray["PLAN_YEAR_MONTH"]            = "年月度";
            $retHeadArray["SLIP_NO"]                    = "伝票番号";
            $retHeadArray["COLLECT_L_CD"]               = "入金科目";
            $retHeadArray["COLLECT_L_NAME"]             = "入金科目名";
            $retHeadArray["COLLECT_M_CD"]               = "入金項目";
            $retHeadArray["COLLECT_M_NAME"]             = "入金項目名";
            $retHeadArray["PAID_LIMIT_DATE"]            = "計画_納入期限";
            $retHeadArray["PLAN_MONEY"]                 = "計画_計画金額";
            $retHeadArray["PLAN_PAID_MONEY_DATE"]       = "入金_入金日";
            $retHeadArray["REDUCED_PLAN_PAID_MONEY"]    = "入金_入金額";
            $retHeadArray["PLAN_PAID_MONEY_DIV"]        = "入金_方法";
            $retHeadArray["PLAN_PAID_MONEY_DIV_NAME"]   = "入金_方法名";
            $retHeadArray["BANKCD"]                     = "金融機関コード";
            $retHeadArray["BANKNAME"]                   = "金融機関名";
            $retHeadArray["KIHON_MONEY"]                = "支援金_支援金基本額";
            $retHeadArray["KASAN_MONEY"]                = "支援金_支援金加算額";
            $retHeadArray["HOJYO_MONEY"]                = "支援金_補助金額";
            $retHeadArray["GENMEN_MONEY"]               = "支援金_学校減免";
            $retHeadArray["SIEN_TOTAL_MONEY"]           = "支援金_合計";
            $retHeadArray["BURDEN_CHARGE"]              = "支援金_学校負担金";
            $retHeadArray["HUTAN_MONEY"]                = "保護者負担額";
        } elseif ($this->field["PATTERN_DIV"] == "2") {
            $retHeadArray["SCHOLARSHIP"]        = "減免種別";
            $retHeadArray["REDUCTION_DIV_NAME"] = "減免種別";
            $retHeadArray["FROM_DATE"]          = "減免開始日";
            $retHeadArray["TO_DATE"]            = "減免終了日";
        } elseif ($this->field["PATTERN_DIV"] == "3") {
            $retHeadArray["DOMI_CD"]       = "寮コード";
            $retHeadArray["DOMI_NAME"]     = "寮名称";
            $retHeadArray["DOMI_ENTDAY"]   = "入寮日";
            $retHeadArray["DOMI_OUTDAY"]   = "退寮日";
            $retHeadArray["TMP_OUT_MONTH"] = "一時退寮月";
        } else {
            if ($this->field["IN_OUT_DIV"] == "1") {
                $retHeadArray["YEAR"]                 = "年度";
                $retHeadArray["IN_OUT_DIV"]           = "収入支出区分";
                $retHeadArray["IN_OUT_DIV_NAME"]      = "収入支出区分名";
                $retHeadArray["REQUEST_NO"]           = "伝票番号";
                $retHeadArray["REQUEST_DATE"]         = "伺い日";
                $retHeadArray["INCOME_DATE"]          = "収入日";
                $retHeadArray["INCOME_APPROVAL"]      = "決裁区分";
                $retHeadArray["INCOME_APPROVAL_NAME"] = "決裁区分名";
                $retHeadArray["COLLECT_L_CD"]         = "入金科目コード";
                $retHeadArray["COLLECT_L_NAME"]       = "入金科目名";
                $retHeadArray["COLLECT_M_CD"]         = "入金項目コード";
                $retHeadArray["COLLECT_M_NAME"]       = "入金項目名";
                $retHeadArray["INCOME_L_CD"]          = "預り金科目（収入）コード";
                $retHeadArray["LEVY_L_NAME"]          = "預り金科目（収入）名";
                $retHeadArray["INCOME_M_CD"]          = "預り金項目（収入）コード";
                $retHeadArray["LEVY_M_NAME"]          = "預り金項目（収入）名";
                $retHeadArray["REQUEST_STAFF"]        = "起案者コード";
                $retHeadArray["STAFFNAME"]            = "起案者名";
                $retHeadArray["REQUEST_GK"]           = "伝票金額";
                $retHeadArray["REQUEST_REASON"]       = "件名";
                $retHeadArray["LINE_NO"]              = "行ＮＯ";
                $retHeadArray["COMMODITY_NAME"]       = "会費名等";
                $retHeadArray["COMMODITY_PRICE"]      = "単価";
                $retHeadArray["COMMODITY_CNT"]        = "数量";
                $retHeadArray["TOTAL_PRICE"]         = "金額";
            } else {
                $retHeadArray["YEAR"]                 = "年度";
                $retHeadArray["IN_OUT_DIV"]           = "収入支出区分";
                $retHeadArray["IN_OUT_DIV_NAME"]      = "収入支出区分名";
                $retHeadArray["REQUEST_NO"]           = "伝票番号";
                $retHeadArray["REQUEST_DATE"]         = "伺い日";
                $retHeadArray["OUTGO_DATE"]           = "支出日";
                $retHeadArray["OUTGO_APPROVAL"]       = "決裁区分";
                $retHeadArray["OUTGO_APPROVAL_NAME"]  = "決裁区分名";
                $retHeadArray["INCOME_L_CD"]          = "預り金科目（収入）コード";
                $retHeadArray["INCOME_L_NAME"]        = "預り金科目（収入）名";
                $retHeadArray["INCOME_M_CD"]          = "預り金項目（収入）コード";
                $retHeadArray["INCOME_M_NAME"]        = "預り金項目（収入）名";
                $retHeadArray["OUTGO_L_CD"]           = "預り金科目（支出）コード";
                $retHeadArray["OUTGO_L_NAME"]         = "預り金科目（支出）名";
                $retHeadArray["OUTGO_M_CD"]           = "預り金項目（支出）コード";
                $retHeadArray["OUTGO_M_NAME"]         = "預り金項目（支出）名";
                $retHeadArray["OUTGO_S_CD"]           = "預り金細目コード";
                $retHeadArray["OUTGO_S_NAME"]         = "預り金細目名";
                $retHeadArray["REQUEST_STAFF"]        = "起案者コード";
                $retHeadArray["STAFFNAME"]            = "起案者名";
                $retHeadArray["REQUEST_GK"]           = "伝票金額";
                $retHeadArray["REQUEST_REASON"]       = "件名";
                $retHeadArray["LINE_NO"]              = "行ＮＯ";
                $retHeadArray["LEVY_S_NAME"]          = "会費名等";
                $retHeadArray["COMMODITY_PRICE"]      = "単価";
                $retHeadArray["COMMODITY_CNT"]        = "数量";
                $retHeadArray["TOTAL_PRICE"]          = "金額";
                $retHeadArray["TOTAL_TAX"]            = "消費税額";
                $retHeadArray["TRADER_CD"]            = "業者コード";
                $retHeadArray["TRADER_NAME"]          = "業者名";
            }
        }
        return $retHeadArray;
    }
}
