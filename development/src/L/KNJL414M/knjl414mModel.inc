<?php

require_once('for_php7.php');

class knjl414mModel extends Model
{
    public $cmd;
    public $examyear;
    public $schoolKind;
    public $examId;

    public function init()
    {
        $this->cmd              = VARS::request("cmd");
        $this->examyear         = CTRL_YEAR + 1;
        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->examId           = VARS::post("EXAM_ID");

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            //機能別に指定された校種を取得
            $this->getSelectSchoolKind();
        }
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["YEAR"] = "年度";
        $header["EXAMNO"] = "統一志願者ID";
        $header["APPLICANT_NAME"] = "入試区分";
        $header["COURSECODENAME"] = "コース";
        $header["FREQUENCY_NAME"] = "回数";
        $header["RECEPTNO"] = "受験番号";
        $header["NAME"] = "氏名";
        $header["NAME_KANA"] = "氏名かな";
        $header["SEX"] = "性別";
        $header["BIRTHDAY"] = "生年月日";
        $header["FINSCHOOL_NAME"] = "出身学校";
        $header["ZIPCD"] = "郵便番号";
        $header["ADDR1"] = "住所1";
        $header["ADDR2"] = "住所2";
        $header["TELNO"] = "電話番号";
        $header["EMAIL"] = "メールアドレス";
        $header["FINISH_DATE"] = "卒業年月日";
        $header["REMARK1"] = "備考1";
        $header["REMARK2"] = "備考2";
        $header["REMARK3"] = "備考3";
        $header["REMARK4"] = "備考4";
        $header["REMARK5"] = "備考5";
        $header["GUARD_NAME"] = "保護者氏名";
        $header["GUARD_NAME_KANA"] = "保護者氏名かな";
        $header["RELATION"] = "続柄";
        $header["GUARD_TELNO"] = "保護者電話番号";
        $header["GUARD_ADDR1"] = "保護者住所1";
        $header["GUARD_ADDR2"] = "保護者住所2";
        $header["GUARD_ZIP"] = "保護者郵便番号";
        $header["SPECIAL_REMARK"] = "内進特記事項";
        $header["SPECIAL_ACT1"] = "内進備考1";
        $header["SPECIAL_ACT2"] = "内進備考2";
        $header["SPECIAL_ACT3"] = "内進備考3";

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ出力
        $dataFlg = false;
        $query = knjl414mQuery::getCsvList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);    //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r", "↓", $row);
            $row = str_replace("\n", "↓", $row);
            $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
            $row["FINISH_DATE"] = str_replace("-", "/", $row["FINISH_DATE"]);

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            $title = $this->examyear."年度";
            $title .= "志願者データCSV出力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
