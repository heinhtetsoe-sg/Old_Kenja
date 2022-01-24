<?php

require_once('for_php7.php');

class knjl115qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl115qModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")     //入試制度
                         );

        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダーセット
        $header = array();
        $header["EXAMNO"]                = "受験番号";
        $header["HR_NAME"]               = "仮クラス";
        $header["ATTEND_NO"]             = "仮クラス番号";
        $header["TESTDIV"]               = "入試区分";
        $header["HCLASS"]                = "クラス";
        $header["HCLASSNO"]              = "番号";
        $header["NAME"]                  = "氏名";
        $header["NAME_KANA"]             = "ふりがな";
        $header["SEX"]                   = "性別";
        $header["STUDENT_NO"]            = "学籍番号";
        $header["FINSCHOOL_NAME"]        = "中学校名";
        $header["ZIPCD"]                 = "生徒郵便番号";
        $header["ADDRESS1"]              = "生徒住所1";
        $header["ADDRESS2"]              = "生徒住所2";
        $header["TELNO"]                 = "生徒電話番号";
        $header["GNAME"]                 = "保護者氏名";
        $header["GZIPCD"]                = "保護者郵便番号";
        $header["GADDRESS1"]             = "保護者住所1";
        $header["GADDRESS2"]             = "保護者住所2";
        $header["GTELNO"]                = "保護者電話番号";
        $header["SAT_NO"]                = "実戦模試受験番号";
        $header["SKIBOU"]                = "スカラ希望";
        $header["SRESULT"]               = "スカラ結果";
        $header["EIGO"]                  = "基礎学力英語得点";
        $header["SUUGAKU"]               = "基礎学力数学得点";
        $header["KOKUGO"]                = "基礎学力国語得点";
        $header["RIKA"]                  = "基礎学力理科得点";
        $header["TOTAL"]                 = "基礎学力合計得点";
        $header["SHOURON"]               = "推薦小論文";
        $header["S_INTERVIEW_VALUE"]     = "推薦面接";
        $header["EIGO_KIJUN"]            = "入試英語得点";
        $header["SUUGAKU_KIJUN"]         = "入試数学得点";
        $header["KOKUGO_KIJUN"]          = "入試国語得点";
        $header["TOTAL_KIJUN"]           = "入試合計得点";
        $header["I_INTERVIEW_VALUE"]     = "入試面接";
        $header["ART1"]                  = "芸術1";
        $header["ART2"]                  = "芸術2";

        $contents = ""; //CSVデータ出力用
        //ヘッダーセット
        $comma = "";
        foreach ($header as $key => $val) {
            $contents .= $comma.$header[$key];
            $comma = ",";
        }
        $contents .= "\n";

        $data = false;
        $query = knjl115qQuery::getCsvData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);
            $row = str_replace("\r","",$row);
            $row = str_replace("\n","",$row);
            $row = str_replace("\r\n","",$row);

            $comma = "";
            foreach ($header as $h_key => $val) {
                $contents .= $comma.$row[$h_key];
                $comma = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $couse    = "作業用データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
