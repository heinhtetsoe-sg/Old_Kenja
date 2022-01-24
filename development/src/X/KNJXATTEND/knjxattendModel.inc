<?php

require_once('for_php7.php');

class knjxattendModel extends Model {

    var $programid;                 //プログラムID
    var $cmd;
    var $grade_combo;
    var $schregno;                  //学籍番号
    var $field          = array();  //出欠の記録修正
    var $requirepresent = array();  //出席しなければならない日数
    var $present        = array();  //出席日数
    var $defalt_annual;
    var $year_add;                  //年度変更フラグ
    var $memo_flg;                  //メモ使用フラグ
    var $mode;                      //在籍卒業
    var $exp_year;                  //左フレームから送られてくる年度

    function knjxattendModel()
    {
        $this->mode = "ungrd";  //在籍モード
    }

    function init()
    {
        $this->memo_flg = VARS::request("MEMO");
        $this->schregno = VARS::request("SCHREGNO");
        $this->exp_year = VARS::request("EXP_YEAR");

        if (VARS::request("PROGRAMID")) {
            $this->programid = VARS::request("PROGRAMID");
        } else {
            $this->programid = "";
        }

        if(VARS::post("GRADE_COMBO")){
            $this->grade_combo = VARS::post("GRADE_COMBO");
        }

        if(VARS::get("ANNUAL") != ""){
            $this->annual = VARS::get("ANNUAL");
        }
        //在籍卒業
        if(VARS::get("mode") != ""){
            $this->mode = VARS::get("mode");
        }
        if (VARS::get("GRD_YEAR")){
            $this->grd_year = VARS::get("GRD_YEAR");           //卒業年度
        }
        if (VARS::get("GRD_SEMESTER")){
            $this->grd_semester = VARS::get("GRD_SEMESTER");           //卒業学期
        }

        //年度変更フラグ
        $this->year_add = VARS::post("year_add");

        for($i=0; $i<2; $i++){
        //出欠の記録修正
            $this->field["SCHOOLCD"][$i]            =  VARS::post("SCHOOLCD".$i);
            $this->field["ANNUAL"][$i]              =  trim(VARS::post("ANNUAL".$i));
            $this->field["ATTENDREC_UPDATED"][$i]   =  VARS::post("attendrec_updated".$i);
            $this->field["HEXAMREMARK_UPDATED"][$i] =  VARS::post("hexamremark_updated".$i);
            $this->field["CLASSDAYS"][$i]           =  VARS::post("CLASSDAYS".$i);
            $this->field["OFFDAYS"][$i]             =  VARS::post("OFFDAYS".$i);
            $this->field["SUSPEND"][$i]             =  VARS::post("SUSPEND".$i);
            $this->field["MOURNING"][$i]            =  VARS::post("MOURNING".$i);
            $this->field["ABROAD"][$i]              =  VARS::post("ABROAD".$i);
            $this->field["SICK"][$i]                =  VARS::post("SICK".$i);
            $this->field["ACCIDENTNOTICE"][$i]      =  VARS::post("ACCIDENTNOTICE".$i);
            $this->field["NOACCIDENTNOTICE"][$i]    =  VARS::post("NOACCIDENTNOTICE".$i);
            $this->field["PRESENT"][$i]             =  VARS::post("PRESENT".$i);
            $this->field["UPDATED_ATTEND"][$i]      =  VARS::post("UPDATED_ATTEND".$i);
        }

        $this->field["ATTENDREC_REMARK"]    =  VARS::post("attendrec_remark");  //備考
        $this->field["UPDATED_MEMO"]        =  VARS::post("UPDATED_MEMO");      //備考変更日付
        $this->defalt_annual                = ($this->grade_combo)? $this->grade_combo : $this->annual ;

        //京都（通信制）フラグ
        $db = Query::dbCheckOut();
        $query = knjxattendQuery::getZ010("00");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->isKyotoTushin = ($row["NAME1"] == 'kyoto' && $row["NAMESPARE3"] == "1") ? "1" : "";
        }
        $result->free();

        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function validateInput()
    {
        //更新時学籍番号のあるかどうかをチェック
        if (!isset($this->schregno) || $this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //年度チェック
        if (!isset($this->grade_combo) || $this->grade_combo == "") {
            $this->setWarning("MSG310","項目：(年度)");
            return false;
        }

        //署名チェック
        if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $db = Query::dbCheckOut();
            $check = $db->getRow(knjxattendQuery::getOpinionsWk($this), DB_FETCHMODE_ASSOC);
            if ($check["CHAGE_OPI_SEQ"]) {
                $this->setWarning("MSG917","（署名済み）");
                return false;
            }
            Query::dbCheckIn($db);
        }

# 2004.08.17 アルプからの修正依頼にて桁数変更
#        if($this->field["ATTENDREC_REMARK"] != "" && (mb_strlen($this->field["ATTENDREC_REMARK"]) > 12 )){
#            $this->setWarning("MSG914","(備考:12文字まで)");
# echo strlen($this->field["ATTENDREC_REMARK"] ) ;
        if (strlen($this->field["ATTENDREC_REMARK"] ) > 49) {
            $this->setWarning("MSG914", "( 出欠の記録備考 ) 49バイトまで");
            return false;
        }

        //UPDATEチェック
        if($this->year_add == "off"){
            $checker = false;
            $db = Query::dbCheckOut();
            $query = knjxattendQuery::getHexamEntremarkDat_Val($this);
            $memo = $db->getRow($query,DB_FETCHMODE_ASSOC); 
            $result = $db->query(knjxattendQuery::getSchregAttendrecDat_Val($this));

            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if($this->field["UPDATED_ATTEND"][$row["SCHOOLCD"]] != $row["UPDATED"]){
                    $checker = true;
                }
            }

            if($this->field["UPDATED_MEMO"] != $memo["UPDATED"]){
                $checker = true;
            }

            $result->free();
            Query::dbCheckIn($db);

            if($checker){
                $this->setWarning("MSG307");
                return false;
            }
        }

        for($i=0; $i<2; $i++){

            //年次データが入力されずに、その他のデータが入力されている場合はエラー表示
            if (( $this->field["ANNUAL"][$i] == null) && ( is_numeric($this->field["CLASSDAYS"][$i])       ||
                is_numeric($this->field["OFFDAYS"][$i])  || is_numeric($this->field["SUSPEND"][$i])        ||
                is_numeric($this->field["MOURNING"][$i]) || is_numeric($this->field["ABROAD"][$i])         ||
                is_numeric($this->field["SICK"][$i])     || is_numeric($this->field["ACCIDENTNOTICE"][$i]) ||
                is_numeric($this->field["NOACCIDENTNOTICE"][$i])) )
            {
                $this->setWarning("MSG301","(年次コード)");
                return false;
            }

            //出席しなければならない日数のチェック
            $requirepresent_num = ( $this->field["CLASSDAYS"][$i] - $this->field["OFFDAYS"][$i])  - 
               ( $this->field["SUSPEND"][$i] + $this->field["MOURNING"][$i] + 
                 $this->field["ABROAD"][$i]);

            if($requirepresent_num < 0 && $this->field["ANNUAL"][$i] != null){
                $this->setWarning("MSG915","出席しなければならない日数がマイナスになっています。");
                return false;
            }else{
                //出席日数のチェック
                $present_num = $requirepresent_num - ($this->field["SICK"][$i] + 
                $this->field["ACCIDENTNOTICE"][$i] + $this->field["NOACCIDENTNOTICE"][$i]);
                if($present_num < 0 && $this->field["ANNUAL"][$i] != null){
                    $this->setWarning("MSG915","(出席日数がマイナスになっています。)");
                    return false;
                }
            }
        }

        return true;
    }

    //詳細
    function &getDetailModel()
    {
        ;
    }

    //出欠の記録修正の更新処理
    function &getUpdateModel()
    {
        if($this->validateInput())
        {
            if(knjxattendQuery::getUpdateQuery($this)){
                $this->setMessage("MSG201");
            }
        }
    }
}
?>
