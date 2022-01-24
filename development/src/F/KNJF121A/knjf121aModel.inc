<?php

require_once('for_php7.php');

class knjf121aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $selectdata;

    function knjf121aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHKIND"  =>  VARS::post("SCHKIND"),  //校種
                             "SDATE"    =>  VARS::post("SDATE"),    //利用期間（開始日付）
                             "EDATE"    =>  VARS::post("EDATE"),    //利用期間（終了日付）
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->field["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectdata          = VARS::post("selectdata");

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjf121aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf121aQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);

        $this->kubunList = array();
        $this->kubunList[] = array('label' => '1:内科',          'value' => '1', 'label2' => '内科');
        $this->kubunList[] = array('label' => '2:外科',          'value' => '2', 'label2' => '外科');
        $this->kubunList[] = array('label' => '3:健康相談',      'value' => '5', 'label2' => '健康相談');
        $this->kubunList[] = array('label' => '4:その他',        'value' => '3', 'label2' => 'その他');
        $this->kubunList[] = array('label' => '5:'.$this->sch_label.'以外',      'value' => '4', 'label2' => $this->sch_label.'以外');
        $this->kubunList[] = array('label' => '9:全て',          'value' => '9', 'label2' => '全て');

        $this->cmd = VARS::request("cmd");
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "月別保健室利用人数集計表";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),3);
        $wrk_sdate = common::DateConv1(str_replace("-","/",$this->field["SDATE"]),1);
        $wrk_edate = common::DateConv1(str_replace("-","/",$this->field["EDATE"]),1);

        //学校名称2表示
        $schoolname2 = "";
        if (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1") {
            $schoolname2 = $db->getOne(knjf121aQuery::getSchoolName2($this))."　　";
        }

        $contents  = ",". $wrk_nendo ."　　". $schoolname2 . $title .",,,,,作成日：". $wrk_date ."\n";
        $contents .= "利用年月 : ". $wrk_sdate ." ～ ". $wrk_edate ."\n\n";

        $type_name = array("1" => "内科",
                           "2" => "外科",
                           "5" => "健康相談",
                           "3" => "その他",
                           "4" => $this->sch_label."以外",
                           "9" => "全て");

        $type = explode (",", $this->selectdata);
        for ($i = 0; $i < get_count($type); $i++){

            //ヘッダの作成//
            $header = array("GRADE"         => "（".$type_name[$type[$i]]."）",
                            "SEX"           => "",
                            "NUM4"          => "4月",
                            "NUM5"          => "5月",
                            "NUM6"          => "6月",
                            "NUM7"          => "7月",
                            "NUM8"          => "8月",
                            "NUM9"          => "9月",
                            "NUM10"         => "10月",
                            "NUM11"         => "11月",
                            "NUM12"         => "12月",
                            "NUM1"          => "1月",
                            "NUM2"          => "2月",
                            "NUM3"          => "3月",
                            "NUM"           => "合計",
                            "REMARK"        => "備考"
                            );

            $contents .= implode(",", $header) ."\n";

            //SQL文発行（名称の取得）
            $query = knjf121aQuery::getSelectName("Z002");
            $result = $db->query($query);
            $opt_name = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_name1[$row["NAMECD2"]] = $row["ABBV1"];
            }
            //出力データの作成//
            //SQL文発行
            $query = knjf121aQuery::selectCsvQuery($this, $type[$i]);
            $result = $db->query($query);
            $data = false;
            $sex = "";
            //小計
            $type_kei = array();
            for($j=0; $j<3; $j++) for($k=0; $k<13; $k++) $type_kei[$j][$k] = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $sex = $row["SEX"];
                $row["SEX"] = ($row["SEX"] == '9') ? '計' : $opt_name1[$row["SEX"]];
                $row["GRADE"] = $db->getOne(knjf121aQuery::getGradeName($row["GRADE"]));

                $contents .= implode(',', $row) ."\n";

                //学年計を計算
                $type_kei[$sex][0]  = $type_kei[$sex][0]  + (int)$row["NUM4"];
                $type_kei[$sex][1]  = $type_kei[$sex][1]  + (int)$row["NUM5"];
                $type_kei[$sex][2]  = $type_kei[$sex][2]  + (int)$row["NUM6"];
                $type_kei[$sex][3]  = $type_kei[$sex][3]  + (int)$row["NUM7"];
                $type_kei[$sex][4]  = $type_kei[$sex][4]  + (int)$row["NUM8"];
                $type_kei[$sex][5]  = $type_kei[$sex][5]  + (int)$row["NUM9"];
                $type_kei[$sex][6]  = $type_kei[$sex][6]  + (int)$row["NUM10"];
                $type_kei[$sex][7]  = $type_kei[$sex][7]  + (int)$row["NUM11"];
                $type_kei[$sex][8]  = $type_kei[$sex][8]  + (int)$row["NUM12"];
                $type_kei[$sex][9]  = $type_kei[$sex][9]  + (int)$row["NUM1"];
                $type_kei[$sex][10] = $type_kei[$sex][10] + (int)$row["NUM2"];
                $type_kei[$sex][11] = $type_kei[$sex][11] + (int)$row["NUM3"];
                $type_kei[$sex][12] = $type_kei[$sex][12] + (int)$row["NUM"];

                $data = true;
            }

            $sex_array = array(1, 2, 9);
            for($j=0; $j<3; $j++) {
                $abbv = ($sex_array[$j] == '9') ? '計' : $opt_name1[$sex_array[$j]];
                //合計の行を出力
                $contents .= "合計," .$abbv.",";
                $contents .= implode(",", (array)$type_kei[$sex_array[$j]]) ."\n";
            }
            $result->free();
            $contents .= "\n";
        }

        Query::dbCheckIn($db);

        if ($data){
            $couse="月別保健室利用人数集計表";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
