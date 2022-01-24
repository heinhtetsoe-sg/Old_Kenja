<?php

require_once('for_php7.php');

class knje373dModel extends Model
{
    public $cmd;
    public $control;
    public $field;

    public function knje373dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        $this->cmd = VARS::request("cmd");

        $this->field = array("HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //CSV出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ
        $header = array("KOKYAKU_NO"       => "顧客整理番号",
                        "NYUSHI_CD"        => "入試制度CD",
                        "JYUKEN_NO"        => "受験番号",
                        "NYUSHI_GAKUBU"    => "入試学部",
                        "NYUSHI_GAKKA1"    => "入試学科1志望",
                        "NAME"             => "志望者氏名（漢字）",
                        "NAME_KANA"        => "志望者氏名（カナ）",
                        "BIRTHDAY"         => "生年月日",
                        "SEX"              => "性別",
                        "GAIKOKUSEKI_DIV"  => "外国籍区分",
                        "FINSCHOOLCD"      => "出身学校",
                        "GRD_DATE"         => "高校卒業年月",
                        "ZIPCD"            => "郵便番号",
                        "PREF_CD"          => "都道府県",
                        "SHITYOSON"        => "市区町村",
                        "CHIMEI"           => "地名地番",
                        "TATEMONO"         => "建物",
                        "TELNO"            => "電話番号",
                        "GUARD_NAME"       => "保証人氏名（漢字）",
                        "GUARD_KANA"       => "保証人氏名（カナ）",
                        "NATIONALITY"      => "国籍コード"
                        );

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }

        //SQL文発行
        $data = false;
        $query = knje373dQuery::getCsvQuery();
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $sep = "";
            foreach ($header as $field => $val) {
                //データセット
                if ($field == "SHITYOSON") {
                    $shityoson = "";
                    $pos = mb_strpos($row["ADDR1"], "　");
                    if ($pos !== false) {
                        $shityoson = mb_substr($row["ADDR1"], $pos + 1);
                    } else {
                        $shityoson = $row["ADDR1"];
                    }
                    $shityoson = mb_substr($shityoson, 0, 16);
                    $contents .= $sep.$shityoson;
                } elseif ($field == "CHIMEI") {
                    $chimei = "";
                    $pos = mb_strpos($row["ADDR2"], "　");
                    if ($pos !== false) {
                        $chimei = mb_substr($row["ADDR2"], 0, $pos);
                    } else {
                        $chimei = $row["ADDR2"];
                    }
                    $chimei = mb_substr($chimei, 0, 20);
                    $contents .= $sep.$chimei;
                } elseif ($field == "TATEMONO") {
                    $tatemono = "";
                    $pos = mb_strpos($row["ADDR2"], "　");
                    if ($pos !== false) {
                        $tatemono = mb_substr($row["ADDR2"], $pos + 1);
                    } else {
                        $tatemono = mb_substr($row["ADDR2"], 20);
                    }
                    $tatemono = mb_substr($tatemono, 0, 20);
                    $contents .= $sep.$tatemono;
                } else {
                    $contents .= $sep.$row[$field];
                }
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $date = str_replace('-', '', CTRL_DATE);
            $title = "suisen_kou_".$date;
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
