<?php

require_once('for_php7.php');

class knjg047Model extends Model {
    var $cmd;
    var $control;
    var $field = array();

    function knjg047Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");
        $this->field = array("DATE_FROM"        =>  VARS::post("DATE_FROM"),        //開始日
                             "DATE_TO"          =>  VARS::post("DATE_TO"),          //終了日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        
        //ヘッダ（2行目）
        $header = array("STAFFCD"               => "職員番号",
                        "STAFFNAME_SHOW"        => "氏名",
                        "SUM_CNT"               => "合計"
                        );
        
        //日付のヘッダーを取得
        $dayHeader = array();
        $query = knjg047Query::getdayHeaderQuery($this);
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dayHeader["DIARY_DATE".$i] = str_replace('-', '/', $row["DIARY_DATE"]);
            $i++;
        }
        $result->free();
        
        //ヘッダ（2行目）セット
        $contents .= implode($header, ",").','. implode($dayHeader, ",") ."\n";
        //SQL文発行
        $data = false;
        //日付データが取得できた場合のみ
        if (get_count($dayHeader) > 0) {        
            $dataTemp = array();
            $query = knjg047Query::getCsvQuery($this, $dayHeader);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                $dataTemp[] = $row;
            }
            $result->free();

            //ヘッダー配列を結合する
            $sum_header = array();
            $sum_header = $header + $dayHeader; 
            
            foreach ($dataTemp as $key => $val) {
                //ヘッダー順で出力
                $sep = "";
                foreach ($sum_header as $hkey => $hval) {
                    //データセット
                    $contents .= $sep.$dataTemp[$key][$hkey];
                    $sep = ",";
                }
                $contents .= "\n";
                $data = true;
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $title = $this->field["DATE_FROM"].'-'.$this->field["DATE_TO"].'_'."補欠集計";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
