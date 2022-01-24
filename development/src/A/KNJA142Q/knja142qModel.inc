<?php

require_once('for_php7.php');

class knja142qModel extends Model
{
    public $field = array();
    public $cmd;

    public function knja142qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SCHOOL_KIND"     =>  VARS::post("SCHOOL_KIND")     //校種
                         );

        $this->cmd = VARS::request("cmd");
    }

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダーセット
        $header = array();
        $header["FINSCHOOL_NAME"]   = "学校名";
        $header["BD003_R1"]         = "入試受験番号";
        $header["BD003_R2"]         = "入試区分";
        $header["INOUT"]            = "内進・外進";
        $header["SCHREGNO"]         = "身分証明書番号（学籍番号）";
        $header["NAME"]             = "氏名";
        $header["NAME_KANA"]        = "ふりがな";
        $header["BIRTHDAY"]         = "生年月日";
        $header["GAIJI_TAIO"]       = "外字対応";
        $header["GAIJI_BIKO"]       = "外字備考";
        $header["ENT_DATE"]         = "入学年月日";
        $header["ISSUER_ZIPCD"]     = "発行者郵便番号";
        $header["ISSUER_ADDR"]      = "発行者住所";
        $header["ISSUER_NAME"]      = "発行者名";
        $header["EXPIRATION_DATE"]  = "有効期限";
        $header["CONTACT_NAME"]     = "連絡先名";
        $header["CONTACT_ADDR"]     = "連絡先住所";
        $header["CONTACT_TELNO"]    = "連絡先電話番";

        //出力固定内容
        $kotei = array();
        if ($this->field["SCHOOL_KIND"] == "J") {
            $kotei["FINSCHOOL_NAME"]   = "駿台甲府中学校";
            $kotei["ISSUER_NAME"]      = "駿台甲府中学校校長";
            $kotei["CONTACT_NAME"]     = "駿台甲府中学校";
        } else {
            $kotei["FINSCHOOL_NAME"]   = "駿台甲府高等学校　普通科";
            $kotei["ISSUER_NAME"]      = "駿台甲府高等学校校長";
            $kotei["CONTACT_NAME"]     = "駿台甲府高等学校　普通科";
        }
        $kotei["ENT_DATE"]             = ""; //空欄
        $kotei["ISSUER_ZIPCD"]         = "〒400-0026";
        $kotei["ISSUER_ADDR"]          = "山梨県甲府市塩部2-8-1";
        $kotei["EXPIRATION_DATE"]      = ""; //空欄
        $kotei["CONTACT_ADDR"]         = "山梨県甲府市塩部2-8-1";
        $kotei["CONTACT_TELNO"]        = "TEL 055-253-6211";
        $koteiHeader = array(); //固定ヘッダ
        foreach ($kotei as $h_key => $val) {
            $koteiHeader[] = $h_key;
        }

        $contents = ""; //CSVデータ出力用

        //ヘッダーセット
        $comma = "";
        foreach ($header as $key => $val) {
            $contents .= $comma.$header[$key];
            $comma = ",";
        }
        $contents .= "\n";

        $data = false;
        $query = knja142qQuery::getCsvData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);

            $gaijiBiko = ""; //外字備考
            $hyphen = "";
            $comma = "";
            foreach ($header as $h_key => $val) {
                $outVal = $row[$h_key]; //取得内容
                if (in_array($h_key, $koteiHeader)) {
                    $outVal = $kotei[$h_key]; //固定内容
                }
                if ($h_key == "BIRTHDAY") {
                    //生年月日
                    $outVal = str_replace("/", "-", $outVal);
                    list($bYear, $bMonth, $bDay) = explode('-', $outVal);
                    //元年を考慮
                    $ganNen = $row["L007_NAMESPARE1"];
                    $nen = $bYear - $ganNen + 1;
                    if ($nen == 1) {
                        $bYear = $row["L007_NAME1"]."元"; // １年は「元年」
                    } else {
                        $bYear = $row["L007_NAME1"].(int)$nen;
                    }
                    $outVal = $bYear."年".(int)$bMonth."月".(int)$bDay."日"; //生年月日を和暦に変換し、出力 (0埋めは不要)
                } elseif ($h_key == "GAIJI_TAIO") {
                    //外字対応
                    $gaiji = "";
                    $text = $row["NAME"];

                    //氏名を1文字ずつ格納
                    $arr = preg_split('//u', $row["NAME"], -1, PREG_SPLIT_NO_EMPTY);
                    foreach ($arr as $ch) {
                        //外字の判定 (正規表現：Unicode 文字プロパティ)
                        if (preg_match('/\p{Co}/u', $ch)) {
                            $ch = mb_convert_encoding($ch, "SJIS-win", "UTF-8"); //文字列を指定コードに変換する
                            $gaijiBiko .= $hyphen.bin2hex($ch); //外字の文字コードを保持
                            $hyphen = " - ";
                        }
                    }

                    if ($gaijiBiko != "") {
                        $outVal = "〇"; //外字が存在する場合
                    }
                } elseif ($h_key == "GAIJI_BIKO") {
                    //外字備考
                    $outVal = $gaijiBiko; //「外字対応」で保持した文字コードを出力
                }
                $contents .= $comma.$outVal; //出力
                $comma = ",";
            }
            
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $couse    = "入学者データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
