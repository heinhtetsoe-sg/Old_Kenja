# kanji=漢字
# $Id: 30deac0d09f1bdb1787c8fed0ece9d709a480009 $

2017/10/24  1.新規作成（駿台中学入試用）
            2.受験番号を4桁から5桁に修正

2018/09/13  1.小学入試用処理を追加

2018/09/14  1.小学入試用処理でログイン校種をみるよう修正

2018/09/20  1.左リストSQL修正

2019/07/18  1.更新・削除の時のチェック処理を修正
            -- MSG308：更新・削除はリストよりデータを選択してから行ってください。
            ※ 原因
            -- 更新・削除の時、リストよりデータを選択しないで行うと
            -- 連番（RECNO）が取得されず、DBエラーとなっていた。
