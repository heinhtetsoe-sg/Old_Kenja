<?php

require_once('for_php7.php');

class knjf120aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjf120aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHKIND" =>  VARS::post("SCHKIND"), //校種
                             "DBNAME"  =>  VARS::post("DBNAME")   //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "保健室利用者の推移";
        $wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        //学校名称2表示
        $schoolname2 = "";
        if (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1") {
            $schoolname2 = $db->getOne(knjf120aQuery::getSchoolName2($this)).",";
        }

        $contents = $wrk_nendo ."," .$schoolname2. $title .",," ."作成日：" .$wrk_date ."\n";

        //ヘッダの作成//
        $header = array("KUBUN"       => "区分",
                        "NINZUU4"     => "4月",
                        "NINZUU5"     => "5月",
                        "NINZUU6"     => "6月",
                        "NINZUU7"     => "7月",
                        "NINZUU8"     => "8月",
                        "NINZUU9"     => "9月",
                        "NINZUU10"    => "10月",
                        "NINZUU11"    => "11月",
                        "NINZUU12"    => "12月",
                        "NINZUU1"     => "1月",
                        "NINZUU2"     => "2月",
                        "NINZUU3"     => "3月",
                        "TOTAL"       => "合計",
                        );

        $contents .= implode($header, ",") ."\n";

        //SQL文発行（性別の取得）
        $query = knjf120aQuery::getSelectName($this);
        $result = $db->query($query);
        $opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_name1[$row["NAMECD"]] = $row["NAME"];
        }

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjf120aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf120aQuery::getSchName($this));
        }
        $sch_label = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //区分一覧
        $kubunList = array();
        $kubunList[]= array('label' => '内科',            'value' => '1');
        $kubunList[]= array('label' => '外科',            'value' => '2');
        $kubunList[]= array('label' => '健康相談活動',    'value' => '5');
        $kubunList[]= array('label' => 'その他',          'value' => '3');
        $kubunList[]= array('label' => $sch_label.'以外', 'value' => '4');

        //出力データの作成//
        //SQL文発行（区分ごと）
        for($j = 0; $j < 5; $j++){
            $query = knjf120aQuery::selectCsvQuery($kubunList[$j]["value"], $this);
            $result = $db->query($query);
            $data = false;
            //月毎の合計（男女）
            $dansei_kei = $josei_kei = array();
            for($i = 0; $i < 12; $i++){
                $dansei_kei[$j][$i] = 0;
                $josei_kei[$j][$i] = 0;
            }
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $sex = substr($row["KUBUN"],0,1);
                $row["KUBUN"] = $kubunList[$j]["label"].$opt_name1[substr($row["KUBUN"],0,1)];
                $contents .= implode($row, ",") ."\n";

                //月毎の合計（男女）を計算
                if($sex == "1"){
                    $dansei_kei[$j][0]  = $dansei_kei[$j][0]  + (int)$row["NINZUU4"];
                    $dansei_kei[$j][1]  = $dansei_kei[$j][1]  + (int)$row["NINZUU5"];
                    $dansei_kei[$j][2]  = $dansei_kei[$j][2]  + (int)$row["NINZUU6"];
                    $dansei_kei[$j][3]  = $dansei_kei[$j][3]  + (int)$row["NINZUU7"];
                    $dansei_kei[$j][4]  = $dansei_kei[$j][4]  + (int)$row["NINZUU8"];
                    $dansei_kei[$j][5]  = $dansei_kei[$j][5]  + (int)$row["NINZUU9"];
                    $dansei_kei[$j][6]  = $dansei_kei[$j][6]  + (int)$row["NINZUU10"];
                    $dansei_kei[$j][7]  = $dansei_kei[$j][7]  + (int)$row["NINZUU11"];
                    $dansei_kei[$j][8]  = $dansei_kei[$j][8]  + (int)$row["NINZUU12"];
                    $dansei_kei[$j][9]  = $dansei_kei[$j][9]  + (int)$row["NINZUU1"];
                    $dansei_kei[$j][10] = $dansei_kei[$j][10] + (int)$row["NINZUU2"];
                    $dansei_kei[$j][11] = $dansei_kei[$j][11] + (int)$row["NINZUU3"];
                    $dansei_kei[$j][12] = $dansei_kei[$j][12] + (int)$row["TOTAL"];
                }
                if($sex == "2"){
                    $josei_kei[$j][0]  = $josei_kei[$j][0]  + (int)$row["NINZUU4"];
                    $josei_kei[$j][1]  = $josei_kei[$j][1]  + (int)$row["NINZUU5"];
                    $josei_kei[$j][2]  = $josei_kei[$j][2]  + (int)$row["NINZUU6"];
                    $josei_kei[$j][3]  = $josei_kei[$j][3]  + (int)$row["NINZUU7"];
                    $josei_kei[$j][4]  = $josei_kei[$j][4]  + (int)$row["NINZUU8"];
                    $josei_kei[$j][5]  = $josei_kei[$j][5]  + (int)$row["NINZUU9"];
                    $josei_kei[$j][6]  = $josei_kei[$j][6]  + (int)$row["NINZUU10"];
                    $josei_kei[$j][7]  = $josei_kei[$j][7]  + (int)$row["NINZUU11"];
                    $josei_kei[$j][8]  = $josei_kei[$j][8]  + (int)$row["NINZUU12"];
                    $josei_kei[$j][9]  = $josei_kei[$j][9]  + (int)$row["NINZUU1"];
                    $josei_kei[$j][10] = $josei_kei[$j][10] + (int)$row["NINZUU2"];
                    $josei_kei[$j][11] = $josei_kei[$j][11] + (int)$row["NINZUU3"];
                    $josei_kei[$j][12] = $josei_kei[$j][12] + (int)$row["TOTAL"];
                }
                $data = true;
            }
            //総合計（男女）を計算
            for($i = 0; $i < 13; $i++){
                $dansei_goukei[$i] = $dansei_goukei[$i] + (int)$dansei_kei[$j][$i];
                $josei_goukei[$i] = $josei_goukei[$i] + (int)$josei_kei[$j][$i];
            }
        }

        //総合計（男女）の行を出力
        $contents .= "合計男" .",";
        $contents .= implode($dansei_goukei, ",") ."\n";
        $contents .= "合計女" .",";
        $contents .= implode($josei_goukei, ",") ."\n";

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse="保健室利用者の推移";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
