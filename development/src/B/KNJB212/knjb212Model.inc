<?php

require_once('for_php7.php');
class knjb212Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjb212Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field["YEAR"] = VARS::post("YEAR");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //CSV出力
    public function getCsvModel()
    {
        //ヘッダ
        $header = array("YEAR"              => "年度",
                        "GRADE"             => "学年",
                        "COURSENAME"        => "課程学科コースコード名",
                        "CLASSNAME"         => "教科名",
                        "SUBCLASSCD"        => "教科・科目コード",
                        "SUBCLASSNAME"      => "科目名",
                        "SUBCLASSABBV"      => "略称名",
                        "CREDITS"           => "単位数",
                        "REQUIRE_FLG_NAME"  => "必履修・選択",
                        "NINTEI"            => "単位認定",
                        "CHOSASHO"          => "調査書",
                        "SHIDOYOROKU"       => "指導要録",
                        "TSUCHISHO"         => "通知書",
                        "SHOMEISHO"         => "証明書",
                        "SOGOGAKUSHU"       => "総合学習",
                        "REMARK"            => "備考",
                        $this->lastColumn   => $this->lastColumn);

        $contents  = "";
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjb212Query::getCsvData($this);
        $result = $db->query($query);
        $data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmp = array();
            //ヘッダ項目のデータをセット
            foreach ($header as $key => $val) {
                if ($key == "REMARK") {
                    $tmp[] = '';
                } elseif ($key == $this->lastColumn) {
                    $tmp[] = $this->lastColumn;
                } else {
                    $tmp[] = $row[$key];
                }
            }

            $contents .= implode($tmp, ",") ."\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $couse = $this->field["YEAR"]."年度_教科・科目・講座情報・登録一覧";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
