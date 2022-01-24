<?php
class knjl099iModel extends Model
{
    public $field  = array();
    public $cmd;
    public $examYear;

    public function init()
    {
        $this->examYear   = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV")
                            );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {

        // 固定値：1
        $fixedValueOne = "1";
        // 固定値：=
        $fixedValueEqual = "=";
        // 固定値：E
        $fixedValueEndMark = "E";
        // 固定値：全角スペース
        $fixedValueFullWidthSpace = "　";

        //志望学科
        $shibouGakka = ($this->field["APPLICANTDIV"] == "2") ? "1700000000" : ($this->field["APPLICANTDIV"] == "1" ? "1400000000" : "");
        
        //ヘッダー
        $header = array("EXAMNO"                 => "受験番号",
                        "KOJIN_ID"               => "個人ID",
                        "GOKAKU_KUBUN"           => "合格区分",
                        "TOKUTAISEI_KUBUN"       => "特待生区分",
                        "SHIBOU_GAKKA"           => "志望学科",
                        "NYUGAKU_NENJI"          => "入学年次",
                        "HONGAKUTAGAKU_KUBUN"    => "本学他学区分",
                        "NYUSHI_SEIDO"           => "入試制度",
                        "OPTIONAL_KUBUN"         => "任意区分",
                        "NAME_1"                 => "正式姓漢字",
                        "NAME_2"                 => "正式中間名漢字",
                        "NAME_3"                 => "正式名漢字",
                        "NAME_KANA_1"            => "正式姓カタカナ",
                        "NAME_KANA_2"            => "正式中間名カタカナ",
                        "NAME_KANA_3"            => "正式名カタカナ",
                        "STAFF_SHITEI_KUBUN"     => "教職員師弟区分",
                        "PAYMENT_ZIP_CODE"       => "納付郵便番号",
                        "PAYMENT_ADDRESS_1"      => "納付住所１",
                        "PAYMENT_ADDRESS_2"      => "納付住所２",
                        "PARENTS_NAME_1"         => "保護者　姓",
                        "PARENTS_NAME_2"         => "保護者　中間",
                        "PARENTS_NAME_3"         => "保護者　名",
                        "INVOICE_KUBUN"          => "請求区分",
                        "BANK_CD"                => "銀行CD",
                        "BRANCH_CD"              => "支店CD",
                        "ACCOUNT_TYPE_CD"        => "口座種別CD",
                        "ACCOUNT_NO"             => "口座番号",
                        "ACCOUNT_HOLDER_KANA"    => "口座名カナ",
                        "ACCOUNT_HOLDER_KANJI"   => "口座名漢字",
                        "CLASS"                  => "クラス",
                        "REFERENCE_NO"           => "整理番号",
                        "CONTACT_TEL"            => "連絡先電話番号",
                        "PAYMENT_ADDRESS_KUBUN"  => "納付書作成住所区分",
                        "PAYMENT_NAME_KUBUN"     => "納付書作成氏名区分",
                        "PARENTS_ZIP_CODE"       => "保護者郵便番号",
                        "PARENTS_ADDRESS_1"      => "保護者住所１",
                        "PARENTS_ADDRESS_2"      => "保護者住所２",
                        "MEMO"                   => "メモ",
                        "OPTIONAL_SETTING_1"     => "任意設定１",
                        "OPTIONAL_SETTING_2"     => "任意設定２",
                        "OPTIONAL_SETTING_3"     => "任意設定３",
                        "OPTIONAL_SETTING_4"     => "任意設定４",
                        "OPTIONAL_SETTING_5"     => "任意設定５",
                        "OPTIONAL_SETTING_6"     => "任意設定６",
                        "OPTIONAL_SETTING_7"     => "任意設定７",
                        "OPTIONAL_SETTING_8"     => "任意設定８",
                        "OPTIONAL_SETTING_9"     => "任意設定９",
                        "OPTIONAL_SETTING_10"    => "任意設定１０",
                        "KYOSEI_NOFU_PATTERN_CD" => "強制納付パターンCD",
                        "END_MARK"               => "エンドマーク"
                        );

        //クリア
        $contents  = "";
        //出力フラグ
        $data = false;

        //ヘッダ
        $contents .= implode(",", $header)."\n";

        $db = Query::dbCheckOut();
        $query = knjl099iQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));

            $examnoPrefix = ($this->field["APPLICANTDIV"] == "2") ? "211700" : ($this->field["APPLICANTDIV"] == "1" ? "111400" : "");
            $examno = $examnoPrefix.$row["EXAMNO"];
            $row["EXAMNO"]                 = $examno;
            $row["KOJIN_ID"]               = $examno;
            $row["GOKAKU_KUBUN"]           = $fixedValueOne;
            $row["TOKUTAISEI_KUBUN"]       = $fixedValueEqual;
            $row["SHIBOU_GAKKA"]           = $shibouGakka;
            $row["NYUGAKU_NENJI"]          = $fixedValueOne;
            $row["OPTIONAL_KUBUN"]         = $fixedValueEqual;
            $row["NYUSHI_SEIDO"]           = substr($examno, 4, 3);
            $row["NAME_2"]                 = $fixedValueEqual;
            $row["NAME_3"]                 = $fixedValueEqual;
            $row["NAME_KANA"]              = mb_substr($row["NAME_KANA"], 0, 20);
            $row["NAME_KANA_2"]            = $fixedValueEqual;
            $row["NAME_KANA_3"]            = $fixedValueEqual;
            $row["STAFF_SHITEI_KUBUN"]     = $fixedValueEqual;
            $row["PAYMENT_ZIP_CODE"]       = $fixedValueEqual;
            $row["PAYMENT_ADDRESS_1"]      = $fixedValueEqual;
            $row["PAYMENT_ADDRESS_2"]      = $fixedValueEqual;
            $row["PARENTS_NAME_1"]         = $fixedValueFullWidthSpace;
            $row["PARENTS_NAME_2"]         = $fixedValueEqual;
            $row["PARENTS_NAME_3"]         = $fixedValueEqual;
            $row["INVOICE_KUBUN"]          = $fixedValueEqual;
            $row["BANK_CD"]                = $fixedValueEqual;
            $row["BRANCH_CD"]              = $fixedValueEqual;
            $row["ACCOUNT_TYPE_CD"]        = $fixedValueEqual;
            $row["ACCOUNT_NO"]             = $fixedValueEqual;
            $row["ACCOUNT_HOLDER_KANA"]    = $fixedValueEqual;
            $row["ACCOUNT_HOLDER_KANJI"]   = $fixedValueEqual;
            $row["CLASS"]                  = $fixedValueEqual;
            $row["REFERENCE_NO"]           = $fixedValueEqual;
            $row["CONTACT_TEL"]            = $fixedValueEqual;
            $row["PAYMENT_ADDRESS_KUBUN"]  = $fixedValueEqual;
            $row["PAYMENT_NAME_KUBUN"]     = $fixedValueEqual;
            $row["PARENTS_ZIP_CODE"]       = $fixedValueEqual;
            $row["PARENTS_ADDRESS_1"]      = $fixedValueEqual;
            $row["PARENTS_ADDRESS_2"]      = $fixedValueEqual;
            $row["MEMO"]                   = $fixedValueEqual;
            $row["OPTIONAL_SETTING_1"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_3"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_4"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_5"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_6"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_7"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_8"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_9"]     = $fixedValueEqual;
            $row["OPTIONAL_SETTING_10"]    = $fixedValueEqual;
            $row["KYOSEI_NOFU_PATTERN_CD"] = $fixedValueEqual;
            $row["END_MARK"]               = $fixedValueEndMark;

            //値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        //改行コードを統一
        $contents = preg_replace("/\r\n|\r|\n/", "\r\n", $contents);

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        // 入試制度名の取得
        $applicantDivName = "";
        $query = knjl099iQuery::getApplicantDivName($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $applicantDivName = $row["NAME1"];
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $appDivValStr = ($this->field["APPLICANTDIV"] == "1") ? "14" : "17";
            $dateStr = date("YmdHis");
            $filename = sprintf("%s_%s_nyushi_Apr.csv", $appDivValStr, $dateStr);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
