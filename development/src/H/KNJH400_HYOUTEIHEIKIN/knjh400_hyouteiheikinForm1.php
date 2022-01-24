<?php

require_once('for_php7.php');

class knjh400_hyouteiheikinForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_hyouteiheikinindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_hyouteiheikinQuery::getName($model));

        $datas = array();
        $annuals = $annualsIdx = array();
        $idx = 0;
        $result2 = $db->query(knjh400_hyouteiheikinQuery::selectQuery($model));
        while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            $annuals[] = $row2['ANNUAL'];
            $annualsIdx[$row2['ANNUAL']] = $idx;
            $arg['gradename'][]['GRADE_NAME2'] = $row2['GRADE_NAME2'];
            $datas[$row2['ANNUAL']]= array();
            $idx++;
        }
        $arg['gradenamecnt'] = get_count($arg['gradename']);
        $result2->free();
        $idx = 0;
        $subdataTemplate = array();
        for ($i = 0; $i < get_count($annuals); $i++) {
            $subdataTemplate[] = '';
        }
        $sum = 0;
        $subdata = $subdataTemplate;
        $value = '';
        $result = $db->query(knjh400_hyouteiheikinQuery::selectQuery2($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($value != $row['VALUE']) {
                if ($value == '' && get_count($annuals) > 1) {
                } else {
                    if (isset($backupRow)) {
                        if ($className == $backupRow['CLASSNAME']) {
                            $backupRow['CLASSNAME'] = '';
                        } else {
                            $className = $backupRow['CLASSNAME'];
                            $now = $idx;
                        }
                        $backupRow['sum'] = $sum;
                        $backupRow['subdata'] = $subdata;
                        $arg['data'][] = $backupRow;
                        $arg['data'][$now]['avedata']+=$ave;
                        $arg['data'][$now]['avecnt']+=$cnt;
                        if ($cnt != 0) {
                            $arg['data'][$now]['ave'] =sprintf("%01.2f", round(($arg['data'][$now]['avedata'] / $arg['data'][$now]['avecnt']) * 100) / 100, 2);
                        }
                        $sum = 0;
                        $ave = 0;
                        $cnt = 0;
                        $subdata = $subdataTemplate;
                        $idx++;
                    }
                }
                $value = $row['VALUE'];
            }
            $sum += intval($row['CREDIT']);
            $ave += intval($row['VALUATION']);
            if (isset($row['VALUATION'])) {
                $subdata[$annualsIdx[$row['ANNUAL']]] += $row['VALUATION'];
            }
            if (isset($row['VALUATION'])) {
                $datas[$row['ANNUAL']][] = $row['VALUATION'];
                $cnt++;
            }
            $backupRow = $row;
        }
        if ($idx != 0) {
            if ($value == '' && get_count($annuals) > 1) {
            } else {
                if ($className == $backupRow['CLASSNAME']) {
                    $backupRow['CLASSNAME'] = '';
                } else {
                    $className = $backupRow['CLASSNAME'];
                    $now = $idx;
                }
                $backupRow['sum'] = $sum;
                $backupRow['subdata'] = $subdata;
                $arg['data'][] = $backupRow;
                $arg['data'][$now]['avedata']+=$ave;
                $arg['data'][$now]['avecnt']+=$cnt;
                if ($cnt != 0) {
                    $arg['data'][$now]['ave'] =sprintf("%01.2f", round(($arg['data'][$now]['avedata'] / $arg['data'][$now]['avecnt']) * 100) / 100, 2);
                }
                $sum = 0;
                $ave = 0;
                $cnt = 0;
                $subdata = $subdataTemplate;
                $idx++;
            }
        }
        $cnt = 0;
        for ($i = 0; $i < get_count($annuals); $i++) {
            $sum = 0;
            for ($j = 0; $j < get_count($datas[$annuals[$i]]); $j++) {
                $sum+=intval($datas[$annuals[$i]][$j]);
                $cnt++;
            }
            $allsum += $sum;
            $arg['avedata'][]['disp'] = $sum.'/'.get_count($datas[$annuals[$i]]);
        }
        if ($cnt != 0) {
            $arg['allave'] = sprintf("%01.2f", round(($allsum / $cnt) * 100) / 100, 2);
        }
        $sum = 0;
        for ($i = 0; $i < get_count($arg['data']); $i++) {
            $sum += intval($arg['data'][$i]['sum']);
        }
        $arg['allsum'] = $sum;
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_hyouteiheikinForm1.html", $arg);
    }
}
