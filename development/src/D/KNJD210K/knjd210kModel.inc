<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd210kModel extends Model {
    var $field      = array();
    var $cmd;
    var $semes_sdate;
    var $semes_edate;
    var $ITEM = array();
    var $SEM = array();
    /**
     * メンバ変数の初期化
     */

    function knjd210kModel()
    {
        $this->field = array();
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }

        //学年
        $this->field["GRADE"] = VARS::post("GRADE");
        //学期
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        //仮評定
        $this->field["KARI_DIV"] = VARS::post("KARI_DIV");
        //仮評定
        $this->field["KARI_NOMIDIV"] = VARS::post("KARI_NOMIDIV");

        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->field["GRADE"] = VARS::get("SEND_GRADE");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd     = VARS::request("cmd");

        //基準日
        $this->ctrl_date = VARS::post("ctrl_date");

        //空の場合
        if (!strlen($this->ctrl_date)) {
            $this->ctrl_date = CTRL_DATE;

        //年度範囲外の場合
        } elseif (strtotime($this->ctrl_date) < strtotime(CTRL_YEAR."/04/01") 
               || strtotime($this->ctrl_date) > strtotime(((int)CTRL_YEAR+1)."/03/31")) {
            $this->ctrl_date = CTRL_DATE;
        }
        $this->ctrl_date = str_replace("/", "-", $this->ctrl_date);

        //項目名
        $this->ITEM=array( "0101"=>"SEM1_INTER_REC",
                           "0102"=>"SEM1_TERM_REC",
                           "0103"=>"SEM1_REC",
                           "0201"=>"SEM2_INTER_REC",
                           "0202"=>"SEM2_TERM_REC",
                           "0203"=>"SEM2_REC",
                           "0302"=>"SEM3_TERM_REC",
                           "8003"=>"GRADE_RECORD",
                           "8013"=>"GRADE_RECORD"
                         );
        //学期
        $this->SEM=array( "0101"=>"1",
                          "0102"=>"1",
                          "0103"=>"1",
                          "0201"=>"2",
                          "0202"=>"2",
                          "0203"=>"2",
                          "0302"=>"3",
                          "8003"=>CTRL_SEMESTER,
                          "8013"=>CTRL_SEMESTER
                        );

        //補点補充用
        $this->ITEM_KARINOMI = array("0103"=>"SEM1_REC_FLG",
                                     "0203"=>"SEM2_REC_FLG",
                                     "8003"=>"SEM3_TERM_REC_FLG"
                                    );

        $this->getPropertiesAll(); //プロパティファイル
    }

    /*  更新処理 */
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->field["SEMESTER"] == "8013") {
            //課題研究(D065)の処理を追加
            //RECORD_PROV_FLG_DATのレコードを削除
            $query = knjd210kQuery::delRecordProvFlgDatD065($this, $this->SEM);
            $db->query($query);
            //評定フラグが仮評定の場合
            if ($this->field["KARI_DIV"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを追加
                $query = knjd210kQuery::insRecordProvFlgDatD065($this, $this->ITEM, $this->SEM);
                $db->query($query);
            }

            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
            return;
        }

        //類型グループHRクラス毎の合計値のクリア
        $query=knjd210kQuery::getTGHDClearQuery($this,$this->ITEM);
        $db->query($query);

        //類型グループHRクラス毎の合計値の算出
        $query=knjd210kQuery::getTGHDCalQuery($db, $this, $this->ITEM, $this->SEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["SUM"] = isset($row["SUM"])? $row["SUM"] : 'Null';
            $row["CNT"] = isset($row["CNT"])? $row["CNT"] : 'Null';
            //類型グループHRクラス毎の合計値の更新
            $query=knjd210kQuery::getTGHDUpQuery($this,$this->ITEM,$row);
            $db->query($query);
        }

        //類型グループ毎の合計値のクリア
        $query=knjd210kQuery::getTGMClearQuery($this,$this->ITEM);
        $db->query($query);

        //類型グループ毎の合計値の算出
        $query=knjd210kQuery::getTGMCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["SUM"] = isset($row["SUM"])? $row["SUM"] : 'Null';
            $row["CNT"] = isset($row["CNT"])? $row["CNT"] : 'Null';
            //類型グループ毎の合計値の更新
            $query=knjd210kQuery::getTGMUpQuery($this,$this->ITEM,$row);
            $db->query($query);
        }

        ///類型評定判定のクリア
        $query=knjd210kQuery::getTASSCDClearQuery($this,$this->ITEM);
        $db->query($query);

        //類型評定の判定
        $query=knjd210kQuery::getTASSCDCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //類型グループ毎の類型評定の更新
            $query=knjd210kQuery::getTASSCDUpQuery($this,$this->ITEM,$row);
            $db->query($query);
        }

        //固定評定処理
        $query=knjd210kQuery::getFixValueSetQuery($this,$this->ITEM);
        $db->query($query);

        //生徒毎の類型評定更新用データクリア
        $query=knjd210kQuery::getSTDClearQuery($this,$this->ITEM,$this->SEM);
        $db->query($query);

        //生徒毎の類型評定更新用データ取得
        $query=knjd210kQuery::getSTDCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //生徒毎の類型評定の更新
            $query=knjd210kQuery::getSTDUpQuery($this,$this->ITEM,$this->SEM,$row);
            $db->query($query);
        }

        $semeSimo2 = substr($this->field["SEMESTER"], 2);
        if ($semeSimo2 == "03") {

            //生徒毎の類型評定更新用データ取得
            $query=knjd210kQuery::getSTDCalQuery($this,$this->ITEM);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //RECORD_PROV_FLG_DATのレコードを削除
                $query = knjd210kQuery::delRecordProvFlgDat($this, $this->ITEM, $this->SEM, $row);
                $db->query($query);
                //評定フラグが仮評定の場合
                if ($this->field["KARI_DIV"] == '1') {
                    //RECORD_PROV_FLG_DATのレコードを追加
                    $query = knjd210kQuery::insRecordProvFlgDat($this, $this->ITEM, $this->SEM, $row);
                    $db->query($query);
                }
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    //科目読替処理
    function &ReplaceSubModel()
    {
        //2005/03/01 追加■科目読替
        if (in_array($this->field["SEMESTER"], array("0103","0203","0302","8003"))) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $tmp = array();
            //読み替えた科目レコード取得
            $query  = knjd210kQuery::ReplaceSubclass($this, $this->SEM[$this->field["SEMESTER"]], $this->ITEM[$this->field["SEMESTER"]]);
            $result = $db->query($query);

            //評定が3パターンできるので重複する項目をひとつにするため整理
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $key = $row["SCHREGNO"].$row["COMBINED_SUBCLASSCD"];
                $tmp[$key]["SCHREGNO"]           = $row["SCHREGNO"];
                if ($this->Properties["useCurriculumcd"] == "1") {
                    $tmp[$key]["COMBINED_CLASSCD"] = $row["COMBINED_CLASSCD"];
                    $tmp[$key]["COMBINED_SCHOOL_KIND"] = $row["COMBINED_SCHOOL_KIND"];
                    $tmp[$key]["COMBINED_CURRICULUM_CD"] = $row["COMBINED_CURRICULUM_CD"];
                }
                $tmp[$key]["COMBINED_SUBCLASSCD"] = $row["COMBINED_SUBCLASSCD"];
                $tmp[$key]["SCORE"]              = $row["SCORE"];
                $tmp[$key]["CHAIRCD"]            = $row["CHAIRCD"];
                $tmp[$key]["EXIST_FLG"]          = $row["EXIST_FLG"];
                if (strlen($row["SCORE"])) {
                    $tmp[$key][$row["TYPE_ASSES_CD"]."_PATTERN_ASSESS"] = $row["TYPE_ASSES_LEVEL"];
/*************** 2005/07/14 DELETE K.AMEKU@OCC *********************
                } else {
                    $tmp[$key]["A_PATTERN_ASSESS"] = "1";
                    $tmp[$key]["B_PATTERN_ASSESS"] = "1";
                    $tmp[$key]["C_PATTERN_ASSESS"] = "1";
*************** 2005/07/14 DELETE K.AMEKU@OCC *********************/
                }
            }

            foreach ($tmp as $key => $row) 
            {
                //読替先の科目レコードが既にある場合はUPDATE
                if (strlen($row["EXIST_FLG"])) {
                    $query = knjd210kQuery::UpdateKinRecord($this, $row, $this->ITEM[$this->field["SEMESTER"]]);
                //新規追加
                } else {
                    $query = knjd210kQuery::AddKinRecord($this, $row, $this->ITEM[$this->field["SEMESTER"]]);
                }
                $db->query($query);

                //1･2年生は3学期期末がない場合の学年平均にNULLにする（更新対象レコードがない場合もある）
                if (in_array($this->field["GRADE"], array("01","02"))) {
                    $db->query(knjd210kQuery::UpdateGradeRec2($db, $this, $this->ITEM, $this->SEM, $row));
                }

#                //得点がある場合のみ学年平均更新
#                if (strlen($row["SCORE"])) {
#                    $db->query(knjd210kQuery::UpdateGradeRec1($this, $row, $this->field["GRADE"]));
#                }
            }

            $db->commit(); //デッドロック防止
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    //追試験データ作成
    function &SuppUpdateModel()
    {
        //学期コンボで学年平均を選択時
        if($this->field["SEMESTER"] == 8003){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //追試験対象者データクリア
            $query=knjd210kQuery::getSTDsplClearQuery($this->field["GRADE"]);
            $db->query($query);

            //追試験対象者データ追加
            $query=knjd210kQuery::getSTDsplInsertQuery($this,$this->field["GRADE"]);
            $db->query($query);

            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    /*  更新処理 */
    function &getUpdateKariNomiModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生徒毎の類型評定更新用データ取得
        $query=knjd210kQuery::getSTDCalQuery($this,$this->ITEM);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //RECORD_PROV_FLG_DATのレコードを削除
            $query = knjd210kQuery::delRecordProvFlgDatKariNomi($this, $this->ITEM, $this->ITEM_KARINOMI, $this->SEM, $row);
            $db->query($query);
            //評定フラグが仮評定の場合
            if ($this->field["KARI_NOMIDIV"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを追加
                $query = knjd210kQuery::insRecordProvFlgDatKariNomi($this, $this->ITEM, $this->ITEM_KARINOMI, $this->SEM, $row);
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

}
//var_dump($_REQUEST);
?>
