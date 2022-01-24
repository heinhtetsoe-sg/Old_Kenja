<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd328Model extends Model {
    var $cmd;
    var $select_data      = array();
    var $jhflg;            //中高判別フラグ
    var $output;
    var $output2;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd328Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->jhflg        = VARS::request("JHFLG");
        $this->output       = VARS::post("OUTPUT");
        $this->output2      = VARS::post("OUTPUT2");
        $this->select_data  = explode(",", VARS::post("selectdata"));
        
        //プロパティファイル
        $this->getPropertiesAll();
        
    }
    //ＣＳＶ出力
    function getDownloadModel()
    {
        $db  = Query::dbCheckOut();

        //グループ名設定
        $groupnm = array();
        $groubif = "";
        $result = $db->query(knjd328Query::get_Coursecode($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($groubif == $row["COURSE_SEQ"].$row["GRADE"]) {
                $groupnm[$row["COURSE_SEQ"].$row["GRADE"]] .= "・".$row["COURSECODENAME"];
            } else {
                $groupnm[$row["COURSE_SEQ"].$row["GRADE"]] = $row["COURSECODENAME"];
            }
            $groubif = $row["COURSE_SEQ"].$row["GRADE"];
        }
        $result->free();

        $contents ="";
        if ($this->output2 == 2) {
            $contents .= "出身学校別成績CSV出力";
        } else {
            $contents  = "塾別成績CSV出力";
        }
        $contents .= "\n";
        for ($i = 0; $i < get_count($this->select_data); $i++) {
            //ヘッダ部分
            //学校又は、塾名取得
            $row = $db->getOne(knjd328Query::Getfspr($this,$this->select_data[$i]));
            $fspsname = $row;
            //和暦変換
            list($y,$m,$d) = explode("-", CTRL_DATE);
            $g = common::Calc_Wareki($y,$m,$d);
            if ($this->output2 == 2) {
                $contents .= "\n";
                $contents .= $g."度　　　　　"."成績追跡調査資料  コード　".$this->select_data[$i]."　　学校名　".$fspsname."　　".$g.$m."月".$d."日";
                $contents .= "\n";
            } else {
                $contents .= "\n";
                $contents .= $g."度　　　　　"."成績追跡調査資料  コード　".$this->select_data[$i]."　　塾名　".$fspsname."　　".$g.$m."月".$d."日";
                $contents .= "\n";
            }

            $datachg  = true;
            $databif  = "";
            $gradebif = "";
            $result = $db->query(knjd328Query::getOutputQueryCsv($this,$this->select_data[$i]));
            $cnt = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($gradebif != $row["GRADE"]) {
                    $cnt = 1;
                }
                if ($databif != $row["GRADE"].$row["COURSE_SEQ"]) {
                    $contents .= "\n";
                    $datachg = true;
                }
                if ($datachg) {
                    $contents .= "第".sprintf("%d",$row["GRADE"])."学年(".$cnt.")".$groupnm[$row["COURSE_SEQ"].$row["GRADE"]];
                    $contents .= "\n";
                    $contents .= "氏名,,組-番,,1中,1期,1学,";//2005.10.27
                    $contents .= "2中,2期,2学,学末,3学,学年,";
                    $contents .= "1実,2実,3実,4実,5実,6実,7実,8実,9実";

                    $contents .= "\n";
                    $datachg = false;
                    $cnt++;
                }
                $contents .= $row["NAME_SHOW"].",".$row["SCHREGNO"].",".$row["HRNO"].",得点,";
                $contents .= $row["SEM1_INTER_REC_TOTAL"].",".$row["SEM1_TERM_REC_TOTAL"].",".$row["SEM1_REC_TOTAL"].",";
                $contents .= $row["SEM2_INTER_REC_TOTAL"].",".$row["SEM2_TERM_REC_TOTAL"].",".$row["SEM2_REC_TOTAL"].",";
                $contents .= $row["SEM3_TERM_REC_TOTAL"].",".$row["SEM3_REC_TOTAL"].",".$row["GRADE_RECORD_TOTAL"].",";
                $contents .= $row["TOTAL1_5"].",".$row["TOTAL2_5"].",".$row["TOTAL3_5"].",";
                $contents .= $row["TOTAL4_5"].",".$row["TOTAL5_5"].",".$row["TOTAL6_5"].",";
                $contents .= $row["TOTAL7_5"].",".$row["TOTAL8_5"].",".$row["TOTAL9_5"];
                $contents .= "\n";

                $contents .= ",,,席次,";
                $contents .= $row["SEM1_INTER_REC_RANK"].",".$row["SEM1_TERM_REC_RANK"].",".$row["SEM1_REC_RANK"].",";
                $contents .= $row["SEM2_INTER_REC_RANK"].",".$row["SEM2_TERM_REC_RANK"].",".$row["SEM2_REC_RANK"].",";
                $contents .= $row["SEM3_TERM_REC_RANK"].",".$row["SEM3_REC_RANK"].",".$row["GRADE_RECORD_RANK"].",";
                $contents .= $row["SCHOOL_PRECEDENCE1_5"].",".$row["SCHOOL_PRECEDENCE2_5"].",".$row["SCHOOL_PRECEDENCE3_5"].",";
                $contents .= $row["SCHOOL_PRECEDENCE4_5"].",".$row["SCHOOL_PRECEDENCE5_5"].",".$row["SCHOOL_PRECEDENCE6_5"].",";
                $contents .= $row["SCHOOL_PRECEDENCE7_5"].",".$row["SCHOOL_PRECEDENCE8_5"].",".$row["SCHOOL_PRECEDENCE9_5"];
                $contents .= "\n";

                $databif  = $row["GRADE"].$row["COURSE_SEQ"];
                $gradebif = $row["GRADE"];
                $data = true;
            }

            $result->free();
            $data = true;
            $contents .= "\n";
            $contents .= "\n";

        }
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->output2 == 2) {
                $filename = sprintf("%s.csv","出身学校別成績一覧");
            } else {
                $filename = sprintf("%s.csv","塾別成績一覧");
            }
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
