<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjm828Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm828Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }
    
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();
        
        //指定年度、日付の和暦表示設定
        list($date1Y, $date1M, $date1D) = preg_split("/-/", CTRL_DATE);
        $nendo1Y = $date1M < "04" ? $date1Y - 1 : $date1Y;
        $date1WarekiNendo = common::Calc_Wareki($nendo1Y, $date1M, $date1D);
        $date1WarekiNen = common::Calc_Wareki($date1Y, $date1M, $date1D);
        $date1WarekiDate = $date1WarekiNen.$date1M."月".$date1D."日";
        
        //ヘッダ配列
        $header = array("連番",
                        "学籍番号",
                        "入学年度",
                        "出席番号",
                        "氏名",
                        "K1",
                        "K2",
                        "K3",
                        "K4",
                        "K5",
                        "K6",
                        "K7",
                        "K8",
                        "K9",
                        "K10",
                        "K11",
                        "K12"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $data = false;
        $count = "10001";
        $dataContents = "";
        $query = knjm828Query::getCsvQuery($this);
        $out = array();
        $current = array();
        $oldSchregno = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($oldSchregno != $row["SCHREGNO"]) {
                if ($oldSchregno) {
                    $dataContents .= $lf;
                }
                $dataContents .= $count . ",";
                $dataContents .= $row["SCHREGNO"]           . ",";
                $dataContents .= ($row["ENT_YEAR"] ? (common::Calc_Wareki($row["ENT_YEAR"], "04", "01")."度") : ""). ",";
                $dataContents .= $row["ATTENDNO"]           . ",";
                $dataContents .= $row["NAME"]               . ",";
            }
            $dataContents .= $row["SUBCLASSCD"]               . ",";
            $oldSchregno = $row["SCHREGNO"];
            
            $count++;
            $data = true;
        }
        $dataContents .= $lf;
        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $title = "";
        $title = $date1WarekiNendo.'度　試験用履修登録一覧'.$lf;
        //サブタイトル
        $subtitle = "";
        $subtitle =  $comma.$comma.$comma.$comma.$comma.$comma.$date1WarekiDate.'現在'.$lf;
        
        $contents  = "";
        $contents .= $title;
        $contents .= $subtitle;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $date1WarekiNendo.'度'. "　試験用履修登録一覧");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    
}
?>

