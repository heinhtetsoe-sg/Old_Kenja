<?php

require_once('for_php7.php');

class knjc010aPopupInfo
{
    public function main(&$model)
    {
        $objForm = new form();
        /* データベース接続 */
        $db = Query::dbCheckOut();

        $query = knjc010aQuery::getPopupInfo1(CTRL_YEAR, CTRL_SEMESTER, $model->popupinfoExecuteDate, $model->popupinfoChaircd, $model->popupinfoPeriod);
        $result = $db->query($query);
        $data = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data[]=array('CHAIRCD' => $row['CHAIRCD'], 'CHAIRNAME' => $row['CHAIRNAME'], 'CREDITS' => $row['CREDITS'], 'ATTESTOR_NAME' => $row['ATTESTOR_NAME']);
            $arg["data"][] = $data;
        }
        $result->free();

        $query = knjc010aQuery::getPopupInfo2(CTRL_YEAR, CTRL_SEMESTER, $model->popupinfoExecuteDate, $model->popupinfoChaircd, $model->popupinfoPeriod);
        $result = $db->query($query);
        $staff = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $staff[$row['CHAIRCD']][] = $row['STAFFNAME_SHOW'];
        }
        $result->free();

        foreach ($data as $key => $value) {
            $data[$key]['STAFF'] = isset($staff[$value['CHAIRCD']]) ? join('<br />', $staff[$value['CHAIRCD']]) : '';
        }
        $arg['data'] = $data;

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjc010aPopupInfo.html", $arg);
    }
}
