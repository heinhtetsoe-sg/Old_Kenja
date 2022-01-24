<?php

require_once('for_php7.php');

class knjl385jQuery extends Query {

    //クラスコンボ
    function getHrClass($model) {
        $query  = "";
        $query .= " SELECT  HR_CLASS,HR_NAME,HR_NAMEABBV ";
        $query .= " FROM    PRE_SCHOOL_HDAT ";
        $query .= " WHERE   YEAR = '".$model->ObjYear."' ";
        $query .= " AND     SEMESTER = '1' ";
        $query .= " AND     GRADE = '01' ";
        $query .= " ORDER BY HR_CLASS ";
        return $query;
    }

    //一覧表示
    function getListPreSchoolInfo($model) {
        // クラス指定ラジオボタンを追加。（　1：仮クラス　2：クラス　）
        if ($model->classDiv == 1) {
            $hrClass  = "T1.PRE_HR_CLASS";
            $attendno = "T1.PRE_ATTENDNO";
        } else {
            $hrClass  = "T1.HR_CLASS";
            $attendno = "T1.ATTENDNO";
        }
        $query  = "";
        $query .= " SELECT  ".$attendno." as PRE_ATTENDNO,T1.EXAMNO,T2.NAME, ";
        $query .= "         (SELECT N1.ABBV1 FROM NAME_MST N1 WHERE N1.NAMECD1='Z002' AND N1.NAMECD2=T2.SEX) AS SEX, ";
        $query .= "         T1.ATTENDFLG1,T1.ATTENDFLG2,T1.SCORE1,T1.SCORE2, ";
        $query .= "         T1.STATIONCD1,T1.STATIONCD2,T1.STATIONCD3,T1.STATIONCD4, ";
        $query .= "         T1.PRE_INFO1,T1.PRE_INFO2,T1.PRE_INFO3, ";
        $query .= "         T1.REMARK ";
        $query .= " FROM    PRE_SCHOOL_INFO_DAT T1 ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR=T1.YEAR AND T2.EXAMNO=T1.EXAMNO ";
        $query .= " WHERE   T1.YEAR = '".$model->ObjYear."' ";
        $query .= " AND     T1.SEMESTER = '1' ";
        $query .= " AND     T1.GRADE = '01' ";
        $query .= " AND     ".$hrClass." = '".$model->pre_hr_class."' ";
        $query .= " ORDER BY ".$attendno.",T1.EXAMNO ";
        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        for ($i=0; $i<get_count($model->data["EXAMNO"]); $i++) {
            $data = array();
            //出欠１・出欠２
            $data["ATTENDFLG1"][TEXT] = $model->fields["ATTENDFLG1"][$i];
            $data["ATTENDFLG2"][TEXT] = $model->fields["ATTENDFLG2"][$i];
            //国語・算数
            $data["SCORE1"][NUMBER] = $model->fields["SCORE1"][$i];
            $data["SCORE2"][NUMBER] = $model->fields["SCORE2"][$i];
            //合計
            if ($model->fields["SCORE1"][$i] != "" && $model->fields["SCORE2"][$i] != "") {
                $data["TOTAL_SCORE"][NUMBER] = (int)$model->fields["SCORE1"][$i] + (int)$model->fields["SCORE2"][$i];
            } else if ($model->fields["SCORE1"][$i] != "") {
                $data["TOTAL_SCORE"][NUMBER] = $model->fields["SCORE1"][$i];
            } else if ($model->fields["SCORE2"][$i] != "") {
                $data["TOTAL_SCORE"][NUMBER] = $model->fields["SCORE2"][$i];
            } else {
                $data["TOTAL_SCORE"][NUMBER] = "";
            }
            //平バス１・平バス２・平バス３・平バス４
            $data["STATIONCD1"][TEXT] = $model->fields["STATIONCD1"][$i];
            $data["STATIONCD2"][TEXT] = $model->fields["STATIONCD2"][$i];
            $data["STATIONCD3"][TEXT] = $model->fields["STATIONCD3"][$i];
            $data["STATIONCD4"][TEXT] = $model->fields["STATIONCD4"][$i];
            //提出書類１・提出書類２・提出書類３
            $data["PRE_INFO1"][TEXT] = $model->fields["PRE_INFO1"][$i];
            $data["PRE_INFO2"][TEXT] = $model->fields["PRE_INFO2"][$i];
            $data["PRE_INFO3"][TEXT] = $model->fields["PRE_INFO3"][$i];
            //備考
            $data["REMARK"][TEXT] = (strlen($remark) > 60) ? substr($model->fields["REMARK"][$i], 0, 60) : $model->fields["REMARK"][$i];
            $data["REGISTERCD"][TEXT] = STAFFCD ;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $where  = " WHERE   YEAR   = '".$model->ObjYear."' ";
            $where .= " AND     EXAMNO = '".$model->data["EXAMNO"][$i]."' ";

            $query = Query::updateSQL($data, "PRE_SCHOOL_INFO_DAT",$where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>