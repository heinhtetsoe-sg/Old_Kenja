<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl341kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $output2;
    var $special_reason_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl341kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->testdiv = VARS::post("TESTDIV");
        $this->output2 = VARS::post("OUTPUT2");//---2005.08.23
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    /**
     * ＣＳＶ出力---NO001
     */
    function getDownloadModel()
    {

        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjl341kQuery::selectCsvQuery($this);
        $result = $db->query($query);

        //header行
        $header1 = array("1"    => "志望区分",
                         "2"    => "志願者数",
                         "3"    => "",
                         "4"    => "",
                         "5"    => "受験者数",
                         "6"    => "",
                         "7"    => "",
                         "8"    => "欠席者数",
                         "9"    => "",
                         "10"   => "",
                         "11"   => "不合格者数",
                         "12"   => "",
                         "13"   => "",
                         "14"   => "合格者数",
                         "15"   => "",
                         "16"   => "",
                         "17"   => " "
                         );
        $header2 = array("1"    => "",
                         "2"    => "男",
                         "3"    => "女",
                         "4"    => "計",
                         "5"    => "男",
                         "6"    => "女",
                         "7"    => "計",
                         "8"    => "男",
                         "9"    => "女",
                         "10"   => "計",
                         "11"   => "男",
                         "12"   => "女",
                         "13"   => "計",
                         "14"   => "コース",
                         "15"   => "男",
                         "16"   => "女",
                         "17"   => "計",
                         );
        $contents  = implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        $data = false;
        $sort_no = "0";
        $kei_abcd = array("A_KEI","B_KEI","C_KEI","D_KEI");
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //志望区分のブレイク時
            if ($sort_no != $row["SORT"]) {
                //小計行を出力
                if ($row["SORT"] == "4" || $row["SORT"] == "6") {
                    //志望区分、志願・受験・欠席・不合格者数
                    $contents .= "小計,";
                    for ($j = 0; $j < 4; $j++) {
                        for ($i = 1; $i < 4; $i++) {
                            $contents .= $shoukei_abcd[$j][$i].",";
                            $goukei_abcd[$j][$i] += $shoukei_abcd[$j][$i];//小計を合計に加算
                            $shoukei_abcd[$j][$i] = 0;//小計をクリア
                        }
                    }
                    //合格者数
                    if ($row["SORT"] == "4") $jsno = 0;
                    if ($row["SORT"] == "6") $jsno = 1;
                    for ($j = $jsno; $j < 3; $j++) {
                        //ブランク
                        if ($j > $jsno) {
                            for ($i = 1; $i < 14; $i++) {
                                $contents .= ",";
                            }
                        }
                        $contents .= $coursename[$j].",";
                        for ($i = 1; $i < 4; $i++) {
                            $contents .= $shoukei_e[$j][$i].",";
                            $goukei_e[$j][$i] += $shoukei_e[$j][$i];//小計を合計に加算
                            $shoukei_e[$j][$i] = 0;//小計をクリア
                        }
                        $contents .= "\n";
                    }
                }
                //志望区分、志願・受験・欠席・不合格者数
                $contents .= $row["ABBV"].",";
                for ($j = 0; $j < 4; $j++) {
                    for ($i = 1; $i < 4; $i++) {
                        $contents .= $row[$kei_abcd[$j].$i].",";
                        $shoukei_abcd[$j][$i] += $row[$kei_abcd[$j].$i];//小計を加算
                        if ($row["SORT"] == "6") 
                            $goukei_abcd[$j][$i] += $row[$kei_abcd[$j].$i];//小計を合計に加算
                    }
                }
            } else {
                //ブランク
                for ($i = 1; $i < 14; $i++) {
                    $contents .= ",";
                }
            }
            //合格者数
            $contents .= $row["EXAMCOURSE_NAME"].",";
            $coursename[$row["MARK"]] = $row["EXAMCOURSE_NAME"];//コース名を保管
            for ($i = 1; $i < 4; $i++) {
                $contents .= $row["E_KEI".$i].",";
                $shoukei_e[$row["MARK"]][$i] += $row["E_KEI".$i];//小計を加算
                if ($row["SORT"] == "6") 
                    $goukei_e[$row["MARK"]][$i] += $row["E_KEI".$i];//小計を合計に加算
            }
            $contents .= "\n";

            //合計行を出力
            if ($row["SORT"] == "6") {
                //志望区分、志願・受験・欠席・不合格者数
                $contents .= "合計,";
                for ($j = 0; $j < 4; $j++) {
                    for ($i = 1; $i < 4; $i++) {
                        $contents .= $goukei_abcd[$j][$i].",";
                    }
                }
                //合格者数
                for ($j = 0; $j < 3; $j++) {
                    //ブランク
                    if ($j > 0) {
                        for ($i = 1; $i < 14; $i++) {
                            $contents .= ",";
                        }
                    }
                    $contents .= $coursename[$j].",";
                    for ($i = 1; $i < 4; $i++) {
                        $contents .= $goukei_e[$j][$i].",";
                        $sougoukei_e[$i] += $goukei_e[$j][$i];//合計を総合計に加算
                    }
                    $contents .= "\n";
                }
                //総合計行を出力
                for ($i = 1; $i < 14; $i++) {
                    $contents .= ",";
                }
                $contents .= "合計,";
                for ($i = 1; $i < 4; $i++) {
                    $contents .= $sougoukei_e[$i].",";
                }
                $contents .= "\n";
            }

            $data = true;
            //試験区分名称
            $testdivname = $row["TEST_NAME"];
            //ソートNo保管
            $sort_no = $row["SORT"];
        }
        $result->free();

        //一般・附属
        $output2name = ($this->output2 == "1") ? "一般" : "附属";
        Query::dbCheckIn($db);

        if($data){
            $filename = $this->ObjYear."年中学校".$testdivname."入学試験合否判定原簿集計表(".$output2name.").csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
