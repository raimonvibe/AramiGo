'use client'

import { useParams } from 'next/navigation'
import { LessonPlayer } from '@/features/lesson'

export default function LessonPage() {
  const params = useParams<{ id: string }>()
  const lessonId = Number(params.id)
  return <LessonPlayer lessonId={lessonId} />
}
