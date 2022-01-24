<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjm839wModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm839wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "TESTCD"           =>  VARS::post("TESTCD"),            //テスト種別
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
        $header = array("教科コード",
                        "教科名",
                        "科目コード",
                        "科目名",
                        "単位",
                        "受講者数",
                        "合格者数",
                        "不合格者数",
                        "不合格者氏名",
                        "保留者数",
                        "保留者氏名",
                        "備考",
                        "修得率"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        $query = knjm839wQuery::getSubclass($this->field["YEAR"], $this->field["GAKKI"], $this->field["TESTCD"]);

        $subclasses = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasses[] = array("CLASSCD" => $row["CLASSCD"],
                                "SCHOOL_KIND" => $row["SCHOOL_KIND"],
                                "CURRICULUM_CD" => $row["CURRICULUM_CD"],
                                "SUBCLASSCD" => $row["SUBCLASSCD"],
                                "CLASSNAME" => $row["CLASSNAME"],
                                "SUBCLASSNAME" => $row["SUBCLASSNAME"]);
        }

        $totalJukoushaSu = 0;
        $totalGoukakushaSu = 0;
        $totalFugoukakushaSu = 0;
//        $totalHoryushaSu = 0;
        //CSVデータを取得し出力変数にセット
        foreach($subclasses as $subclass) {
            $fugou = array();
            $query1 = knjm839wQuery::getSubclassFugou($this->field["YEAR"], $this->field["GAKKI"], $this->field["TESTCD"], $subclass["CLASSCD"], $subclass["SCHOOL_KIND"], $subclass["CURRICULUM_CD"], $subclass["SUBCLASSCD"]);

            $result1 = $db->query($query1);
            $jukoushaSu = 0;
            $gokakushaSu = 0;
            $fugokakushaSu = 0;
            $names = "";
            $hasdata = false;
            while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
                $names = "";
                $names .= $row1["CLASSCD"]           .$comma;
                $names .= $subclass["CLASSNAME"]     .$comma;
                $names .= $row1["SUBCLASSCD"]        .$comma;
                $names .= $subclass["SUBCLASSNAME"]  .$comma;
                $names .= $row1["CREDITS"]           .$comma;
                $names .= $row1["JUKOUSHA_SU"]       .$comma;
                $names .= $row1["GOUKAKUSHA_SU"]     .$comma;
                $names .= $row1["FUGOUKAKUSHA_SU"];
                $jukoushaSu = $row1["JUKOUSHA_SU"];
                $gokakushaSu = $row1["GOUKAKUSHA_SU"];
                $fugokakushaSu = $row1["FUGOUKAKUSHA_SU"];
                if ($row1["SCHREGNO"] || $row1["NAME"]) {
                    $fugou[] = $row1["SCHREGNO"]."-".$row1["NAME"].";";
                }
                $hasdata = true;
            }
            if (!$hasdata) {
                $names = ',,,,,0,0,0';
            }
            $totalJukoushaSu += $jukoushaSu;
            $totalGoukakushaSu += $gokakushaSu;
            $totalFugoukakushaSu += $fugokakushaSu;
            $result1->free();

            $fugout = "";
            for ($i = 0; $i < get_count($fugou); $i++) {
                $fugout .= $fugou[$i];
            }
            $percentage = "";
            if ($jukoushaSu > 0) {
                $percentage = round(intval(100 * (100.0 * $gokakushaSu / $jukoushaSu)) / 100.0, 1)."%";
            }
            $dataContents .= $names. $comma . $fugout . $comma . $comma . $comma . $comma . $percentage .$lf;
            $data = true;
        }

        // 合計
        $goukei = "";
        $goukei .= $comma;
        $goukei .= "合計".$comma;
        $goukei .= $comma;
        $goukei .= $comma;
        $goukei .= $comma;
        $goukei .= $totalJukoushaSu . $comma;
        $goukei .= $totalGoukakushaSu . $comma;
        $goukei .= $totalFugoukakushaSu . $comma;
        $goukei .= $comma;
        $goukei .= $comma;
        $goukei .= $comma;
        $goukei .= $comma;
        if ($totalJukoushaSu > 0) {
            $goukei .= round(intval(100 * (100.0 * $totalGoukakushaSu / $totalJukoushaSu)) / 100.0, 1)."%" .$comma;
        } else {
            $goukei .= ''  .$comma;
        }
        $goukei .= $lf;
        $dataContents .= $goukei;

        // 前年の実績
        $query12 = knjm839wQuery::getSubclassFugou($this->field["YEAR"] - 1, '', '', '', '', '', '');
        $result12 = $db->query($query12);
        $fugokakushaSu = 0;
        $names = "";
        $hasdata = false;
        while ($row12 = $result12->fetchRow(DB_FETCHMODE_ASSOC)) {
            $names = "";
            $names .= ''  .$comma;
            $names .= '前年の実績'  .$comma;
            $names .= ''  .$comma;
            $names .= ''  .$comma;
            $names .= ''  .$comma;
            $names .= $row1["JUKOUSHA_SU"]       .$comma;
            $names .= $row1["GOUKAKUSHA_SU"]     .$comma;
            $names .= $row1["FUGOUKAKUSHA_SU"];
            $names .= ''  .$comma;
            $names .= ''  .$comma;
            $names .= ''  .$comma;
            $names .= ''  .$comma;
            if ($row1["JUKOUSHA_SU"] > 0) {
                $names = round(intval(100 * (100.0 * $row1["GOUKAKUSHA_SU"] / $row1["JUKOUSHA_SU"])) / 100.0, 1)."%" .$comma;
            } else {
                $names .= ''  .$comma;
            }
            $hasdata = true;
        }
        if (!$hasdata) {
            $names = ',前年の実績,,,,0,0,0,,,,,0%';
        }
        $result12->free();

        $zennen = $names. $comma .$comma .$comma .$lf;
        $dataContents .= $zennen;

        $dataContents .= $lf;
        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $title = "";
        $title = $comma.$comma.$comma.$comma.$date1WarekiNendo.'度　単位認定会議資料'.$lf;
        //サブタイトル
        $subtitle = "";
        $subtitle =  $comma.$comma.$comma.$comma.$comma.$comma.$comma.$comma.$comma.$comma.$date1WarekiDate.'現在'.$lf;
        
        $contents  = "";
        $contents .= $title;
        $contents .= $subtitle;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $date1WarekiNendo.'度'. "　単位認定会議資料");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    
}
?>

